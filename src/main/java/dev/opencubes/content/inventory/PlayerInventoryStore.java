package dev.opencubes.content.inventory;

import dev.opencubes.util.ServerLevels;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.slf4j.Logger;

/**
 * Writes compressed inventory dumps under {@code <world>/opencubes/inventories/} and restores
 * them for {@code /opencubes inventory}.
 */
public final class PlayerInventoryStore {

    public static final PlayerInventoryStore INSTANCE = new PlayerInventoryStore();

    public static final String TAG_PLAYER_NAME = "PlayerName";
    public static final String TAG_PLAYER_UUID = "PlayerUUID";
    public static final String TAG_INVENTORY = "Inventory";
    public static final String TAG_XP = "XP";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PREFIX = "inventory-";
    private static final Pattern SAFE_CHARS = Pattern.compile("[^A-Za-z0-9_-]");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

    private PlayerInventoryStore() {}

    public Path storePlayerInventory(ServerPlayer player, String type) {
        Inventory inventory = player.getInventory();
        ItemStackHandler copy = copyInventory(inventory);
        return storeHandler(copy, player.getGameProfile().name(), type, ServerLevels.of(player), meta -> {
            meta.putString(TAG_PLAYER_NAME, player.getGameProfile().name());
            meta.putString(TAG_PLAYER_UUID, player.getGameProfile().id().toString());
            meta.putDouble("X", player.getX());
            meta.putDouble("Y", player.getY());
            meta.putDouble("Z", player.getZ());
        }, player.registryAccess());
    }

    public Path storeHandler(ItemStackHandler inventory, String name, String type, ServerLevel level,
                             Consumer<CompoundTag> extras, HolderLookup.Provider registries) {
        String safeName = SAFE_CHARS.matcher(name).replaceAll("_");
        Path file = newDumpFile(level.getServer(), safeName, type);
        TagValueOutput output = TagValueOutput.createWithContext(new ProblemReporter.Collector(), registries);
        inventory.serialize(output.child(TAG_INVENTORY));
        CompoundTag root = output.buildResult();
        root.putLong("Created", System.currentTimeMillis());
        root.putString("Type", type);
        extras.accept(root);

        try {
            Files.createDirectories(file.getParent());
            NbtIo.writeCompressed(root, file);
            LOGGER.info("Stored inventory dump for {} as {}", name, file.getFileName());
        } catch (IOException e) {
            LOGGER.warn("Failed to dump inventory for {} into {}", name, file, e);
        }
        return file;
    }

    public boolean restoreInventory(ServerPlayer player, String fileId) {
        CompoundTag root = loadTag(ServerLevels.of(player).getServer(), fileId);
        if (root == null || !root.contains(TAG_INVENTORY)) {
            return false;
        }

        ItemStackHandler stored = new ItemStackHandler(0);
        TagValueInput.create(new ProblemReporter.Collector(), player.registryAccess(), root)
                .child(TAG_INVENTORY)
                .ifPresent(stored::deserialize);

        Inventory inventory = player.getInventory();
        inventory.clearContent();
        int size = Math.min(stored.getSlots(), inventory.getContainerSize());
        for (int i = 0; i < size; i++) {
            inventory.setItem(i, stored.getStackInSlot(i));
        }
        for (int i = size; i < stored.getSlots(); i++) {
            ItemStack leftover = stored.getStackInSlot(i);
            if (!leftover.isEmpty()) {
                player.drop(leftover, false);
            }
        }
        return true;
    }

    public List<String> listDumpIds(MinecraftServer server, String prefix) {
        Path folder = inventoriesFolder(server);
        List<String> result = new ArrayList<>();
        if (!Files.isDirectory(folder)) {
            return result;
        }
        String needle = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder, PREFIX + "*.dat")) {
            for (Path path : stream) {
                String id = stripFilename(path.getFileName().toString());
                if (needle.isEmpty() || id.toLowerCase(Locale.ROOT).startsWith(needle)
                        || (PREFIX + id).toLowerCase(Locale.ROOT).startsWith(needle)) {
                    result.add(id);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to list inventory dumps", e);
        }
        result.sort(String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    public static String stripFilename(String name) {
        String stripped = name;
        if (stripped.regionMatches(true, 0, PREFIX, 0, PREFIX.length())) {
            stripped = stripped.substring(PREFIX.length());
        }
        if (stripped.toLowerCase(Locale.ROOT).endsWith(".dat")) {
            stripped = stripped.substring(0, stripped.length() - 4);
        }
        return stripped;
    }

    private CompoundTag loadTag(MinecraftServer server, String fileId) {
        Path file = inventoriesFolder(server).resolve(PREFIX + stripFilename(fileId) + ".dat");
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            return NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            LOGGER.warn("Failed to read inventory dump {}", file, e);
            return null;
        }
    }

    private synchronized Path newDumpFile(MinecraftServer server, String player, String type) {
        String date = DATE_FORMAT.format(new Date());
        Path folder = inventoriesFolder(server);
        int id = 0;
        while (true) {
            Path candidate = folder.resolve(String.format(Locale.ROOT, PREFIX + "%s-%s-%s-%d.dat",
                    player, date, type, id));
            if (!Files.exists(candidate)) {
                return candidate;
            }
            id++;
        }
    }

    private static Path inventoriesFolder(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("opencubes").resolve("inventories");
    }

    private static ItemStackHandler copyInventory(Inventory inventory) {
        ItemStackHandler copy = new ItemStackHandler(inventory.getContainerSize());
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            copy.setStackInSlot(i, inventory.getItem(i).copy());
        }
        return copy;
    }

    /** Builds a handler from a list of stacks (grave loot). */
    public static ItemStackHandler fromStacks(List<ItemStack> stacks) {
        ItemStackHandler handler = new ItemStackHandler(Math.max(1, stacks.size()));
        int slot = 0;
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                if (slot >= handler.getSlots()) {
                    ItemStackHandler grown = new ItemStackHandler(handler.getSlots() + 16);
                    for (int i = 0; i < handler.getSlots(); i++) {
                        grown.setStackInSlot(i, handler.getStackInSlot(i));
                    }
                    handler = grown;
                }
                handler.setStackInSlot(slot++, stack.copy());
            }
        }
        return handler;
    }
}
