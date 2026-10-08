package dev.opencubes.content.button;

import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/**
 * The thirteen materials a big button comes in, one per vanilla button.
 *
 * <p>Upstream shipped two, because 1.12 vanilla had two. The set type carries the click
 * sounds and whether arrows can press the button, so nothing else has to be said per
 * material. The texture is vanilla's own, which is why thirteen variants cost no art.
 *
 * @param id      registry path, without the {@code _big_button} suffix
 * @param setType vanilla's block set type, source of sounds and arrow behaviour
 * @param texture the vanilla texture the model points at
 */
public record BigButtonMaterial(String id, BlockSetType setType, String texture) {

    private static BigButtonMaterial wood(String id, BlockSetType setType) {
        return new BigButtonMaterial(id, setType, "minecraft:block/" + id + "_planks");
    }

    public static final List<BigButtonMaterial> ALL = List.of(
            wood("oak", BlockSetType.OAK),
            wood("spruce", BlockSetType.SPRUCE),
            wood("birch", BlockSetType.BIRCH),
            wood("jungle", BlockSetType.JUNGLE),
            wood("acacia", BlockSetType.ACACIA),
            wood("dark_oak", BlockSetType.DARK_OAK),
            wood("mangrove", BlockSetType.MANGROVE),
            wood("cherry", BlockSetType.CHERRY),
            new BigButtonMaterial("bamboo", BlockSetType.BAMBOO, "minecraft:block/bamboo_planks"),
            new BigButtonMaterial("crimson", BlockSetType.CRIMSON, "minecraft:block/crimson_planks"),
            new BigButtonMaterial("warped", BlockSetType.WARPED, "minecraft:block/warped_planks"),
            new BigButtonMaterial("stone", BlockSetType.STONE, "minecraft:block/stone"),
            new BigButtonMaterial("polished_blackstone", BlockSetType.POLISHED_BLACKSTONE,
                    "minecraft:block/polished_blackstone")
    );

    public String blockId() {
        return id + "_big_button";
    }

    /** The vanilla button this one is crafted from, four at a time. */
    public net.minecraft.world.level.block.Block vanillaButton() {
        return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(
                net.minecraft.resources.Identifier.withDefaultNamespace(id + "_button"));
    }

    /** Whether this material behaves as wood: quieter, and pressable by arrows. */
    public boolean isWooden() {
        return setType.canButtonBeActivatedByArrows();
    }

    /** Base properties, copied from the vanilla button of the same material. */
    public net.minecraft.world.level.block.state.BlockBehaviour.Properties properties() {
        return net.minecraft.world.level.block.state.BlockBehaviour.Properties
                .ofFullCopy(isWooden() ? Blocks.OAK_BUTTON : Blocks.STONE_BUTTON);
    }
}
