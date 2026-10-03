package dev.opencubes.content.luggage;

import dev.opencubes.registry.OCEntities;
import dev.opencubes.registry.OCItems;
import dev.opencubes.config.OCCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class LuggageEntity extends TamableAnimal {

    public static final int SIZE_NORMAL = 27;
    public static final int SIZE_SPECIAL = 54;

    private static final EntityDataAccessor<Integer> DATA_SIZE =
            SynchedEntityData.defineId(LuggageEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VIEWERS =
            SynchedEntityData.defineId(LuggageEntity.class, EntityDataSerializers.INT);

    private ItemStackHandler inventory = createHandler(SIZE_NORMAL);
    private boolean special;
    /** Client lid animation, 0 closed to 1 open, like {@code ChestLidController}. */
    private float lidOpenness;
    private float lidOpennessO;

    public LuggageEntity(EntityType<? extends LuggageEntity> type, Level level) {
        super(type, level);
        setTame(true, false);
    }

    public LuggageEntity(Level level) {
        this(OCEntities.LUGGAGE.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    private static ItemStackHandler createHandler(int size) {
        return new ItemStackHandler(size);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SIZE, SIZE_NORMAL);
        builder.define(DATA_VIEWERS, 0);
    }

    public void startOpen() {
        entityData.set(DATA_VIEWERS, entityData.get(DATA_VIEWERS) + 1);
    }

    public void stopOpen() {
        int viewers = Math.max(0, entityData.get(DATA_VIEWERS) - 1);
        entityData.set(DATA_VIEWERS, viewers);
        if (viewers == 0) {
            level().playSound(null, blockPosition(), SoundEvents.CHEST_CLOSE, SoundSource.NEUTRAL,
                    0.5F, level().random.nextFloat() * 0.1F + 0.9F);
        }
    }

    public float lidOpenness(float partialTick) {
        return Mth.lerp(partialTick, lidOpennessO, lidOpenness);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0D,
                OCCommonConfig.LUGGAGE_FOLLOW_START.get().floatValue(),
                OCCommonConfig.LUGGAGE_FOLLOW_STOP.get().floatValue()));
        goalSelector.addGoal(3, new LuggageCollectGoal(this));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public boolean isSpecial() {
        return special || entityData.get(DATA_SIZE) > SIZE_NORMAL;
    }

    public void setSpecial() {
        if (special) {
            return;
        }
        special = true;
        ItemStackHandler expanded = createHandler(SIZE_SPECIAL);
        for (int i = 0; i < inventory.getSlots(); i++) {
            expanded.setStackInSlot(i, inventory.getStackInSlot(i));
        }
        inventory = expanded;
        entityData.set(DATA_SIZE, SIZE_SPECIAL);
    }

    public ItemStackHandler tryInsert(ItemStack stack) {
        ItemStack remainder = ItemHandlerHelper.insertItem(inventory, stack, false);
        stack.setCount(remainder.getCount());
        return inventory;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.NAME_TAG)) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            ItemStack item = convertToItem();
            if (player.getInventory().add(item)) {
                discard();
                level().playSound(null, blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL,
                        0.5F, level().random.nextFloat() * 0.1F + 0.9F);
            }
        } else {
            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new LuggageMenu(id, inv, this),
                    getDisplayName()),
                    buf -> buf.writeVarInt(getId()));
            level().playSound(null, blockPosition(), SoundEvents.CHEST_OPEN, SoundSource.NEUTRAL,
                    0.5F, level().random.nextFloat() * 0.1F + 0.9F);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack getPickResult() {
        return convertToItem();
    }

    public ItemStack convertToItem() {
        ItemStack stack = new ItemStack(OCItems.LUGGAGE.get());
        LuggageItem.saveInventory(stack, inventory, special);
        if (hasCustomName()) {
            stack.set(DataComponents.CUSTOM_NAME, getCustomName());
        }
        return stack;
    }

    public void restoreFromStack(ItemStack stack) {
        LuggageItem.InventoryData data = LuggageItem.loadInventory(stack);
        inventory = data.handler();
        special = data.special();
        entityData.set(DATA_SIZE, inventory.getSlots());
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name != null) {
            setCustomName(name);
        }
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
        setSpecial();
        super.thunderHit(level, lightning);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void setHealth(float health) {
        // Invulnerable companion - keep at full.
        super.setHealth(getMaxHealth());
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (OCCommonConfig.LUGGAGE_PLAY_WALKING_SOUND.get()) {
            super.playStepSound(pos, state);
        }
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Special", special);
        tag.put("Inventory", inventory.serializeNBT(registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        special = tag.getBoolean("Special");
        int size = special ? SIZE_SPECIAL : SIZE_NORMAL;
        inventory = createHandler(size);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registryAccess(), tag.getCompound("Inventory"));
        }
        entityData.set(DATA_SIZE, inventory.getSlots());
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            int size = entityData.get(DATA_SIZE);
            if (inventory.getSlots() != size) {
                inventory = createHandler(size);
            }
            lidOpennessO = lidOpenness;
            float target = entityData.get(DATA_VIEWERS) > 0 ? 1.0F : 0.0F;
            lidOpenness = Mth.approach(lidOpenness, target, 0.1F);
        }
    }
}
