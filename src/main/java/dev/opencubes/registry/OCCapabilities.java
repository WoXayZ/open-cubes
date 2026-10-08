package dev.opencubes.registry;

import dev.opencubes.OCConstants;
import dev.opencubes.content.automation.BlockPlacerBlockEntity;
import dev.opencubes.content.automation.ItemDropperBlockEntity;
import dev.opencubes.content.button.BigButtonBlockEntity;
import dev.opencubes.content.sprinkler.SprinklerBlockEntity;
import dev.opencubes.util.FluidHandlerBridge;
import dev.opencubes.util.ItemHandlerBridge;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class OCCapabilities {

    private OCCapabilities() {}

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        // NeoForge only wires this up for plain BucketItem instances, so our subclass needs it
        // registering by hand - without it no tank or machine can see what the bucket holds.
        event.registerItem(
                Capabilities.Fluid.ITEM,
                (stack, context) -> FluidHandlerBridge.asResource(new FluidBucketWrapper(stack)),
                OCItems.XP_BUCKET.get());

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                OCBlockEntities.BIG_BUTTON.get(),
                (blockEntity, side) -> ItemHandlerBridge.asResource(new InvWrapper((BigButtonBlockEntity) blockEntity)));

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                OCBlockEntities.TANK.get(),
                (blockEntity, side) -> fluids(blockEntity.getFluidHandler(side)));

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                OCBlockEntities.XP_BOTTLER.get(),
                (blockEntity, side) -> fluids(blockEntity.getFluidHandler(side)));

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                OCBlockEntities.XP_BOTTLER.get(),
                (blockEntity, side) -> items(blockEntity.getItemHandler(side)));

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                OCBlockEntities.BLOCK_PLACER.get(),
                (blockEntity, side) -> ItemHandlerBridge.asResource(((BlockPlacerBlockEntity) blockEntity).getItems()));

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                OCBlockEntities.ITEM_DROPPER.get(),
                (blockEntity, side) -> ItemHandlerBridge.asResource(((ItemDropperBlockEntity) blockEntity).getItems()));

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                OCBlockEntities.VACUUM_HOPPER.get(),
                (blockEntity, side) -> items(blockEntity.getItemHandler(side)));

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                OCBlockEntities.VACUUM_HOPPER.get(),
                (blockEntity, side) -> fluids(blockEntity.getFluidHandler(side)));

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                OCBlockEntities.AUTO_ANVIL.get(),
                (blockEntity, side) -> items(blockEntity.getItemHandler(side)));
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                OCBlockEntities.AUTO_ANVIL.get(),
                (blockEntity, side) -> fluids(blockEntity.getFluidHandler(side)));

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                OCBlockEntities.AUTO_ENCHANTMENT_TABLE.get(),
                (blockEntity, side) -> items(blockEntity.getItemHandler(side)));
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                OCBlockEntities.AUTO_ENCHANTMENT_TABLE.get(),
                (blockEntity, side) -> fluids(blockEntity.getFluidHandler(side)));

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                OCBlockEntities.HEALER.get(),
                (be, side) -> FluidHandlerBridge.asResource(be.tank()));

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                OCBlockEntities.SPRINKLER.get(),
                (be, side) -> side == null || side == Direction.DOWN
                        ? FluidHandlerBridge.asResource(((SprinklerBlockEntity) be).tank())
                        : null);
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                OCBlockEntities.SPRINKLER.get(),
                (be, side) -> ItemHandlerBridge.asResource(((SprinklerBlockEntity) be).items()));
    }

    @Nullable
    private static ResourceHandler<ItemResource> items(@Nullable IItemHandler handler) {
        return handler == null ? null : ItemHandlerBridge.asResource(handler);
    }

    @Nullable
    private static ResourceHandler<FluidResource> fluids(@Nullable IFluidHandler handler) {
        return handler == null ? null : FluidHandlerBridge.asResource(handler);
    }
}
