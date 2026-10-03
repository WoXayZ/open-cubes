package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import dev.opencubes.content.automation.AutoAnvilBlockEntity;
import dev.opencubes.content.automation.AutoEnchantmentTableBlockEntity;
import dev.opencubes.content.automation.BlockPlacerBlockEntity;
import dev.opencubes.content.automation.ItemDropperBlockEntity;
import dev.opencubes.content.automation.VacuumHopperBlockEntity;
import dev.opencubes.content.button.BigButtonBlockEntity;
import dev.opencubes.content.tank.TankBlockEntity;
import dev.opencubes.content.xp.XpBottlerBlockEntity;
import dev.opencubes.content.healer.HealerBlockEntity;
import dev.opencubes.content.sprinkler.SprinklerBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class OCCapabilities {

    private OCCapabilities() {}

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        // NeoForge only wires this up for plain BucketItem instances, so our subclass needs it
        // registering by hand - without it no tank or machine can see what the bucket holds.
        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, context) -> new FluidBucketWrapper(stack),
                OCItems.XP_BUCKET.get());

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                OCBlockEntities.BIG_BUTTON.get(),
                (blockEntity, side) -> new InvWrapper((BigButtonBlockEntity) blockEntity));

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                OCBlockEntities.TANK.get(),
                TankBlockEntity::getFluidHandler);

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                OCBlockEntities.XP_BOTTLER.get(),
                XpBottlerBlockEntity::getFluidHandler);

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                OCBlockEntities.XP_BOTTLER.get(),
                XpBottlerBlockEntity::getItemHandler);

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                OCBlockEntities.BLOCK_PLACER.get(),
                (blockEntity, side) -> ((BlockPlacerBlockEntity) blockEntity).getItems());

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                OCBlockEntities.ITEM_DROPPER.get(),
                (blockEntity, side) -> ((ItemDropperBlockEntity) blockEntity).getItems());

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                OCBlockEntities.VACUUM_HOPPER.get(),
                VacuumHopperBlockEntity::getItemHandler);

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                OCBlockEntities.VACUUM_HOPPER.get(),
                VacuumHopperBlockEntity::getFluidHandler);

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                OCBlockEntities.AUTO_ANVIL.get(),
                AutoAnvilBlockEntity::getItemHandler);
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                OCBlockEntities.AUTO_ANVIL.get(),
                AutoAnvilBlockEntity::getFluidHandler);

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                OCBlockEntities.AUTO_ENCHANTMENT_TABLE.get(),
                AutoEnchantmentTableBlockEntity::getItemHandler);
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                OCBlockEntities.AUTO_ENCHANTMENT_TABLE.get(),
                AutoEnchantmentTableBlockEntity::getFluidHandler);

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                OCBlockEntities.HEALER.get(),
                (be, side) -> ((HealerBlockEntity) be).tank());

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                OCBlockEntities.SPRINKLER.get(),
                (be, side) -> side == null || side == net.minecraft.core.Direction.DOWN
                        ? ((SprinklerBlockEntity) be).tank()
                        : null);
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                OCBlockEntities.SPRINKLER.get(),
                (be, side) -> ((SprinklerBlockEntity) be).items());
    }
}
