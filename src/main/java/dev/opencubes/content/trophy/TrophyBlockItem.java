package dev.opencubes.content.trophy;

import dev.opencubes.OCConstants;
import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCRegistries;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class TrophyBlockItem extends BlockItem {

    /** Shipped datapack ids under data/opencubes/opencubes/trophy/ - used if the registry lookup is empty. */
    private static final List<String> BUILTIN_IDS = List.of(
            "allay", "armadillo", "axolotl", "bat", "bee", "blaze", "bogged", "breeze", "camel", "cat",
            "cave_spider", "chicken", "cod", "cow", "creeper", "dolphin", "donkey", "drowned",
            "elder_guardian", "ender_dragon", "enderman", "endermite", "evoker", "fox", "frog", "ghast",
            "giant", "glow_squid", "goat", "guardian", "hoglin", "horse", "husk", "illusioner",
            "iron_golem", "llama", "magma_cube", "mooshroom", "mule", "ocelot", "panda", "parrot",
            "phantom", "pig", "piglin", "piglin_brute", "pillager", "polar_bear", "pufferfish", "rabbit",
            "ravager", "salmon", "sheep", "shulker", "silverfish", "skeleton", "skeleton_horse", "slime",
            "sniffer", "snow_golem", "spider", "squid", "stray", "strider", "tadpole", "trader_llama",
            "tropical_fish", "turtle", "vex", "villager", "vindicator", "wandering_trader", "warden",
            "witch", "wither", "wither_skeleton", "wolf", "zoglin", "zombie", "zombie_horse",
            "zombie_villager", "zombified_piglin");

    public TrophyBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack create(Block block, Identifier trophyId) {
        ItemStack stack = new ItemStack(block);
        stack.set(OCDataComponents.TROPHY_ID.get(), trophyId);
        return stack;
    }

    /** Every trophy variant for the creative tab (registry first, then builtin fallback). */
    public static void fillCreative(HolderLookup.Provider holders, Consumer<ItemStack> output) {
        Set<Identifier> ids = new LinkedHashSet<>();
        holders.lookup(OCRegistries.TROPHY).ifPresent(lookup ->
                lookup.listElements().forEach(holder -> ids.add(holder.key().identifier())));
        if (ids.isEmpty()) {
            for (String path : BUILTIN_IDS) {
                ids.add(OCConstants.id(path));
            }
        }
        Block block = OCBlocks.TROPHY.get();
        for (Identifier id : ids) {
            output.accept(create(block, id));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        Identifier trophyId = stack.get(OCDataComponents.TROPHY_ID.get());
        if (trophyId == null) {
            return super.getName(stack);
        }
        return Component.translatable(getDescriptionId() + ".entity", entityLabel(trophyId));
    }

    public static Component nameFor(Holder.Reference<TrophyDefinition> holder) {
        Component entityName = BuiltInRegistries.ENTITY_TYPE.getOptional(holder.value().entity())
                .map(EntityType::getDescription)
                .orElseGet(() -> Component.literal(holder.key().identifier().getPath()));
        return Component.translatable("block.opencubes.trophy.entity", entityName);
    }

    private static Component entityLabel(Identifier trophyId) {
        // Shipped trophies use the mob path as their datapack id; full entity id lives in JSON.
        Identifier guess = Identifier.fromNamespaceAndPath("minecraft", trophyId.getPath());
        return BuiltInRegistries.ENTITY_TYPE.getOptional(guess)
                .map(EntityType::getDescription)
                .orElseGet(() -> Component.literal(trophyId.getPath()));
    }
}
