package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Zero-defence helmet material with no armour texture layers. Glasses use a custom face layer
 * for the item model; empty layers keep {@code HumanoidArmorLayer} from drawing a plate helmet.
 */
public final class OCArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> REGISTRY =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, OCConstants.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GLASSES =
            REGISTRY.register("glasses", () -> {
                Map<ArmorItem.Type, Integer> defence = new EnumMap<>(ArmorItem.Type.class);
                for (ArmorItem.Type type : ArmorItem.Type.values()) {
                    defence.put(type, 0);
                }
                return new ArmorMaterial(
                        defence,
                        1,
                        SoundEvents.ARMOR_EQUIP_LEATHER,
                        () -> Ingredient.EMPTY,
                        List.of(),
                        0.0F,
                        0.0F);
            });

    private OCArmorMaterials() {}

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
