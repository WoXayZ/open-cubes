package dev.opencubes.content.target;

import dev.opencubes.registry.OCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class ArcheryTargetBlockEntity extends BlockEntity {

    private int strength;
    private int decayTicks;

    public ArcheryTargetBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.ARCHERY_TARGET.get(), pos, state);
    }

    public int signalStrength() {
        return strength;
    }

    public void onHit(Vec3 hit) {
        if (level == null || level.isClientSide()) {
            return;
        }
        int next = ArcheryTargetBlock.accuracyStrength(worldPosition, getBlockState(), hit);
        strength = next;
        decayTicks = 40;
        setChanged();
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, 3);
        level.updateNeighborsAt(worldPosition, state.getBlock());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ArcheryTargetBlockEntity target) {
        if (target.decayTicks > 0) {
            target.decayTicks--;
            if (target.decayTicks == 0 && target.strength > 0) {
                target.strength = 0;
                target.setChanged();
                level.updateNeighborsAt(pos, state.getBlock());
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putInt("Strength", strength);
        tag.putInt("Decay", decayTicks);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        strength = tag.getIntOr("Strength", 0);
        decayTicks = tag.getIntOr("Decay", 0);
    }
}
