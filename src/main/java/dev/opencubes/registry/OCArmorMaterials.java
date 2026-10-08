package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * Zero-defence helmet material with no armour texture layers. Glasses use a custom face layer
 * for the item model; an asset with no texture keeps the humanoid layer from drawing a plate helmet.
 */
public final class OCArmorMaterials {

    public static final ArmorMaterial GLASSES = new ArmorMaterial(
            1,
            Map.of(
                    ArmorType.BOOTS, 0,
                    ArmorType.LEGGINGS, 0,
                    ArmorType.CHESTPLATE, 0,
                    ArmorType.HELMET, 0,
                    ArmorType.BODY, 0),
            1,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F,
            0.0F,
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(OCConstants.MOD_ID, "repairs_glasses")),
            ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(OCConstants.MOD_ID, "glasses")));

    private OCArmorMaterials() {}
}
