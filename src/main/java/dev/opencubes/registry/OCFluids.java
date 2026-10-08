package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import dev.opencubes.fluid.XpJuiceFluidType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class OCFluids {

    private static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, OCConstants.MOD_ID);
    private static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, OCConstants.MOD_ID);
    private static final DeferredRegister.Blocks FLUID_BLOCKS =
            DeferredRegister.createBlocks(OCConstants.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> XP_JUICE_TYPE =
            FLUID_TYPES.register("xp_juice", XpJuiceFluidType::new);

    public static final DeferredHolder<Fluid, FlowingFluid> XP_JUICE =
            FLUIDS.register("xp_juice", () -> new BaseFlowingFluid.Source(xpJuiceProperties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_XP_JUICE =
            FLUIDS.register("flowing_xp_juice", () -> new BaseFlowingFluid.Flowing(xpJuiceProperties()));

    /**
     * World block that backs the fluid, so an XP bucket can be emptied onto the ground like any
     * other bucket. It lives here rather than in {@code OCBlocks} because it has no block item,
     * no loot table and no recipe - it only exists as the fluid's physical form.
     */
    public static final DeferredBlock<LiquidBlock> XP_JUICE_BLOCK = FLUID_BLOCKS.registerBlock("xp_juice",
            properties -> new LiquidBlock(XP_JUICE.get(), properties),
            properties -> properties
                    .mapColor(MapColor.EMERALD)
                    .replaceable()
                    .noCollision()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .liquid()
                    .lightLevel(state -> 10)
                    .sound(SoundType.EMPTY));

    private OCFluids() {}

    private static BaseFlowingFluid.Properties xpJuiceProperties() {
        return new BaseFlowingFluid.Properties(XP_JUICE_TYPE, XP_JUICE, FLOWING_XP_JUICE)
                .bucket(OCItems.XP_BUCKET)
                .block(XP_JUICE_BLOCK);
    }

    public static void register(IEventBus modBus) {
        FLUID_TYPES.register(modBus);
        FLUIDS.register(modBus);
        FLUID_BLOCKS.register(modBus);
    }
}
