package dev.opencubes.content.sprinkler;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.util.FluidHandlerBridge;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.FarmlandWaterManager;
import net.neoforged.neoforge.common.ticket.AABBTicket;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.joml.Quaternionf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.joml.Vector3f;

public class SprinklerBlockEntity extends BlockEntity implements MenuProvider {

    public static int capacityMb() {
        return OCCommonConfig.SPRINKLER_INTERNAL_TANK.get();
    }

    private final FluidTank tank = new FluidTank(capacityMb(),
            stack -> stack.getFluid().isSame(net.minecraft.world.level.material.Fluids.WATER)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final ItemStackHandler items = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(Items.BONE_MEAL);
        }
    };

    private boolean enabled;
    private int ticks;
    @Nullable
    private AABBTicket hydrationTicket;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> tank.getFluidAmount();
                case 1 -> tank.getCapacity();
                case 2 -> enabled ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return 3;
        }
    };

    public SprinklerBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.SPRINKLER.get(), pos, state);
    }

    public ItemStackHandler items() {
        return items;
    }

    public FluidTank tank() {
        return tank;
    }

    public ContainerData data() {
        return data;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static final float MAX_TILT_DEGREES = 35.0F;
    /** Arm and nozzle centre in block space, matching {@code sprinkler_arm.json}. */
    public static final float PIVOT_Y = 3.0F / 16.0F;

    /** Tilt of the arm around its own long axis; it rocks left and right like the 1.12 sprinkler. */
    public float getArmTilt(float partialTick) {
        if (!enabled) {
            return 0.0F;
        }
        return Mth.sin((ticks + partialTick) * 0.04F) * MAX_TILT_DEGREES;
    }

    /**
     * Arm orientation: the model's arm lies along Z (north), it is turned to the block axis and
     * then tilted. The renderer and the spray share this so the water leaves where the arm points.
     */
    public static Quaternionf armRotation(Direction facing, float tiltDegrees) {
        Quaternionf rotation = new Quaternionf();
        if (facing.getAxis() == Direction.Axis.X) {
            rotation.rotateY(Mth.HALF_PI);
        }
        return rotation.rotateZ(tiltDegrees * Mth.DEG_TO_RAD);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SprinklerBlockEntity sprinkler) {
        sprinkler.ticks++;

        if (level.isClientSide()) {
            return;
        }

        int capacity = capacityMb();
        if (sprinkler.tank.getCapacity() != capacity) {
            sprinkler.tank.setCapacity(capacity);
        }

        pullWater(level, pos, sprinkler);

        if (sprinkler.ticks % OCCommonConfig.SPRINKLER_WATER_CONSUME_RATE.get() == 0) {
            FluidStack drained = sprinkler.tank.drain(1, IFluidHandler.FluidAction.EXECUTE);
            sprinkler.setEnabled(!drained.isEmpty());
        }

        if (!sprinkler.enabled) {
            return;
        }

        ServerLevel server = (ServerLevel) level;
        sprinkler.spawnSprayParticles(server);
        sprinkler.attemptFertilize(server);
        sprinkler.attemptRandomGrowth(server);

        if (sprinkler.ticks % 40 == 0) {
            level.playSound(null, pos, SoundEvents.WEATHER_RAIN, SoundSource.BLOCKS, 0.2F, 1.5F);
        }
    }

    private void spawnSprayParticles(ServerLevel level) {
        Direction facing = getBlockState().getValue(SprinklerBlock.FACING);
        float tilt = getArmTilt(0.0F);
        Vector3f nozzle = armRotation(facing, tilt).transform(new Vector3f(0.0F, 1.0F, 0.0F));
        Vector3f along = armRotation(facing, 0.0F).transform(new Vector3f(0.0F, 0.0F, 1.0F));
        float horizontal = Mth.sqrt(nozzle.x * nozzle.x + nozzle.z * nozzle.z);
        if (horizontal < 1.0E-3F) {
            return;
        }
        float sideX = nozzle.x / horizontal;
        float sideZ = nozzle.z / horizontal;
        float strength = 0.12F + 0.28F * horizontal / Mth.sin(MAX_TILT_DEGREES * Mth.DEG_TO_RAD);

        double originX = worldPosition.getX() + 0.5D;
        double originY = worldPosition.getY() + PIVOT_Y + 0.08D;
        double originZ = worldPosition.getZ() + 0.5D;
        for (int i = 0; i < 6; i++) {
            double outlet = (level.getRandom().nextDouble() - 0.5D) * 0.7D;
            double speed = strength * (0.6D + level.getRandom().nextDouble() * 0.4D);
            double scatter = (level.getRandom().nextDouble() - 0.5D) * 0.06D;
            // count 0 sends the offsets as the exact velocity. Splash only keeps that
            // horizontal speed when the vertical one is zero, then lifts the drop itself.
            level.sendParticles(ParticleTypes.SPLASH,
                    originX + along.x * outlet, originY, originZ + along.z * outlet,
                    0, sideX * speed + along.x * scatter, 0.0D, sideZ * speed + along.z * scatter, 1.0D);
        }
    }

    private static void pullWater(Level level, BlockPos pos, SprinklerBlockEntity sprinkler) {
        if (sprinkler.tank.getSpace() <= 0) {
            return;
        }
        var found = level.getCapability(Capabilities.Fluid.BLOCK, pos.below(), Direction.UP);
        if (found == null) {
            var below = level.getFluidState(pos.below());
            if (below.isSource() && below.is(net.minecraft.world.level.material.Fluids.WATER)) {
                int sip = Math.min(sprinkler.tank.getSpace(), 50);
                if (sip > 0) {
                    sprinkler.tank.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, sip),
                            IFluidHandler.FluidAction.EXECUTE);
                }
            }
            return;
        }
        IFluidHandler below = FluidHandlerBridge.asTanks(found);
        FluidStack sim = below.drain(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, sprinkler.tank.getSpace()),
                IFluidHandler.FluidAction.SIMULATE);
        if (sim.isEmpty() || !sim.getFluid().isSame(net.minecraft.world.level.material.Fluids.WATER)) {
            return;
        }
        int filled = sprinkler.tank.fill(sim, IFluidHandler.FluidAction.SIMULATE);
        if (filled > 0) {
            FluidStack taken = below.drain(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, filled),
                    IFluidHandler.FluidAction.EXECUTE);
            sprinkler.tank.fill(taken, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void setEnabled(boolean value) {
        if (enabled == value) {
            return;
        }
        enabled = value;
        setChanged();
        updateHydration();
        sync();
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void updateHydration() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        if (hydrationTicket != null) {
            hydrationTicket.invalidate();
            hydrationTicket = null;
        }
        if (enabled) {
            int range = OCCommonConfig.SPRINKLER_RANGE.get();
            AABB box = new AABB(worldPosition).inflate(range, 1.0D, range);
            hydrationTicket = FarmlandWaterManager.addAABBTicket(server, box);
        }
    }

    private boolean hasBonemeal() {
        for (int i = 0; i < items.getSlots(); i++) {
            if (items.getStackInSlot(i).is(Items.BONE_MEAL)) {
                return true;
            }
        }
        return false;
    }

    private boolean consumeBonemeal() {
        for (int i = 0; i < items.getSlots(); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (stack.is(Items.BONE_MEAL)) {
                items.extractItem(i, 1, false);
                return true;
            }
        }
        return false;
    }

    private void attemptFertilize(ServerLevel level) {
        boolean boost = hasBonemeal();
        int chance = boost
                ? OCCommonConfig.SPRINKLER_BONEMEAL_FERTILIZE_CHANCE.get()
                : OCCommonConfig.SPRINKLER_FERTILIZE_CHANCE.get();
        if (chance <= 0 || level.getRandom().nextInt(chance) != 0) {
            return;
        }

        int range = OCCommonConfig.SPRINKLER_RANGE.get();
        int attempts = boost ? 8 : 3;
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = level.getRandom().nextInt(2 * range + 1) - range;
            int z = level.getRandom().nextInt(2 * range + 1) - range;
            for (int y = -1; y <= 2; y++) {
                BlockPos target = worldPosition.offset(x, y, z);
                BlockState targetState = level.getBlockState(target);
                if (targetState.getBlock() instanceof BonemealableBlock growable
                        && growable.isValidBonemealTarget(level, target, targetState)) {
                    if (growable.isBonemealSuccess(level, level.getRandom(), target, targetState)
                            || boost) {
                        growable.performBonemeal(level, level.getRandom(), target, targetState);
                        if (boost) {
                            consumeBonemeal();
                        }
                        return;
                    }
                }
            }
        }
    }

    /**
     * Extra crop ticks while active so farmland hydration is not the only growth effect.
     */
    private void attemptRandomGrowth(ServerLevel level) {
        if (ticks % 10 != 0) {
            return;
        }
        int range = OCCommonConfig.SPRINKLER_RANGE.get();
        int x = level.getRandom().nextInt(2 * range + 1) - range;
        int z = level.getRandom().nextInt(2 * range + 1) - range;
        for (int y = -1; y <= 2; y++) {
            BlockPos target = worldPosition.offset(x, y, z);
            BlockState targetState = level.getBlockState(target);
            if (targetState.getBlock() instanceof CropBlock
                    || targetState.getBlock() instanceof BonemealableBlock) {
                targetState.randomTick(level, target, level.getRandom());
                return;
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (enabled) {
            updateHydration();
        }
    }

    @Override
    public void setRemoved() {
        if (hydrationTicket != null) {
            hydrationTicket.invalidate();
            hydrationTicket = null;
        }
        super.setRemoved();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.opencubes.sprinkler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new SprinklerMenu(id, inventory, this, data);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putChild("Tank", tank);
        items.serialize(tag.child("Items"));
        tag.putBoolean("Enabled", enabled);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        if (tag.keySet().contains("Tank")) {
            tag.child("Tank").ifPresent(tank::deserialize);
        }
        if (tag.keySet().contains("Items")) {
            tag.child("Items").ifPresent(items::deserialize);
        }
        enabled = tag.getBooleanOr("Enabled", false);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
