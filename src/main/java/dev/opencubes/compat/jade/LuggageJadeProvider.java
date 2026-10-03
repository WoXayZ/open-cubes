package dev.opencubes.compat.jade;



import dev.opencubes.OCConstants;

import dev.opencubes.content.luggage.LuggageEntity;

import net.minecraft.nbt.CompoundTag;

import net.minecraft.network.chat.Component;

import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.items.ItemStackHandler;

import snownee.jade.api.EntityAccessor;

import snownee.jade.api.IEntityComponentProvider;

import snownee.jade.api.IServerDataProvider;

import snownee.jade.api.ITooltip;

import snownee.jade.api.config.IPluginConfig;



public enum LuggageJadeProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

    INSTANCE;



    public static final ResourceLocation UID = OCConstants.id("luggage");



    private static final String TAG_SPECIAL = "Special";

    private static final String TAG_FILLED = "Filled";

    private static final String TAG_CAPACITY = "Capacity";



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

    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {

        CompoundTag data = accessor.getServerData();

        if (!data.contains(TAG_CAPACITY)) {

            return;

        }

        if (data.getBoolean(TAG_SPECIAL)) {

            tooltip.add(Component.translatable("opencubes.jade.luggage.special"));

        }

        tooltip.add(Component.translatable("opencubes.jade.luggage.slots",

                data.getInt(TAG_FILLED), data.getInt(TAG_CAPACITY)));

    }



    @Override

    public ResourceLocation getUid() {

        return UID;

    }

}


