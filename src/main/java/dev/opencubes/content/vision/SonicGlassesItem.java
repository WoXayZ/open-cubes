package dev.opencubes.content.vision;

import dev.opencubes.registry.OCArmorMaterials;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;

public class SonicGlassesItem extends ArmorItem {

    public SonicGlassesItem(Properties properties) {
        super(OCArmorMaterials.GLASSES, Type.HELMET, properties.stacksTo(1));
    }

    public static boolean isWearing(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof SonicGlassesItem;
    }
}
