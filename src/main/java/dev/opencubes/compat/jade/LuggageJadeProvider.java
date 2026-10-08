package dev.opencubes.compat.jade;

import dev.opencubes.OCConstants;
import dev.opencubes.content.luggage.LuggageEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.items.ItemStackHandler;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum LuggageJadeProvider implements IEntityComponentProvider {
    INSTANCE;

    public static final Identifier UID = OCConstants.id("luggage");

    private static final String TAG_SPECIAL = "Special";
    private static final String TAG_FILLED = "Filled";
    private static final String TAG_CAPACITY = "Capacity";

    public enum Data implements IServerDataProvider<EntityAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, EntityAccessor accessor) {
            if (!(accessor.getEntity() instanceof LuggageEntity luggage)) {
                return;
            }
            ItemStackHandler inventory = luggage.getInventory();
            int filled = 0;
            for (int i = 0; i < inventory.getSlots(); i++) {
                if (!inventory.getStackInSlot(i).isEmpty()) {
                    filled++;
                }
            }
            data.putBoolean(TAG_SPECIAL, luggage.isSpecial());
            data.putInt(TAG_FILLED, filled);
            data.putInt(TAG_CAPACITY, inventory.getSlots());
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(TAG_CAPACITY)) {
            return;
        }
        if (data.getBooleanOr(TAG_SPECIAL, false)) {
            tooltip.add(Component.translatable("opencubes.jade.luggage.special"));
        }
        tooltip.add(Component.translatable("opencubes.jade.luggage.slots",
                data.getIntOr(TAG_FILLED, 0), data.getIntOr(TAG_CAPACITY, 0)));
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
