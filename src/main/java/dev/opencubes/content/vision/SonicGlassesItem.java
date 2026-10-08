package dev.opencubes.content.vision;

import dev.opencubes.registry.OCArmorMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

public class SonicGlassesItem extends Item {

    public SonicGlassesItem(Properties properties) {
        super(properties.stacksTo(1).humanoidArmor(OCArmorMaterials.GLASSES, ArmorType.HELMET));
    }

    public static boolean isWearing(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof SonicGlassesItem;
    }
}
