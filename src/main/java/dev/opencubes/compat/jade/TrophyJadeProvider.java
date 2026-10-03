package dev.opencubes.compat.jade;

import dev.opencubes.OCConstants;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum TrophyJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final String TAG_COOLDOWN = "Cooldown";
    public static final ResourceLocation UID = OCConstants.id("trophy");

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof TrophyBlockEntity trophy) {
            data.putInt(TAG_COOLDOWN, trophy.getCooldown());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof TrophyBlockEntity trophy)) {
            return;
        }
        Optional<TrophyDefinition> definition = trophy.definition();
        if (definition.isEmpty()) {
            ResourceLocation id = trophy.getTrophyId();
            if (id != null) {
                tooltip.add(Component.translatable("opencubes.jade.trophy.type", id.getPath()));
            }
            appendCooldown(tooltip, accessor);
            return;
        }

        TrophyDefinition def = definition.get();
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(def.entity());
        Component typeName = entityType != null
                ? entityType.getDescription()
                : Component.literal(def.entity().toString());
        tooltip.add(Component.translatable("opencubes.jade.trophy.type", typeName));

        appendCooldown(tooltip, accessor);

        def.drop().ifPresent(drop -> {
            Item item = BuiltInRegistries.ITEM.get(drop.item());
            if (item == null) {
                return;
            }
            ItemStack stack = new ItemStack(item, drop.count());
            tooltip.add(Component.translatable("opencubes.jade.trophy.drop",
                    drop.count(), stack.getHoverName()));
        });
    }

    private static void appendCooldown(ITooltip tooltip, BlockAccessor accessor) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(TAG_COOLDOWN)) {
            return;
        }
        int cooldown = data.getInt(TAG_COOLDOWN);
        if (cooldown <= 0) {
            tooltip.add(Component.translatable("opencubes.jade.trophy.ready"));
        } else {
            float seconds = cooldown / 20.0F;
            tooltip.add(Component.translatable("opencubes.jade.trophy.cooldown",
                    String.format("%.1f", seconds)));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
