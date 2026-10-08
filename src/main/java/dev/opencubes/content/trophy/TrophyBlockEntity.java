package dev.opencubes.content.trophy;

import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCRegistries;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class TrophyBlockEntity extends BlockEntity {

    private static final String TAG_TROPHY = "Trophy";
    private static final String TAG_COOLDOWN = "Cooldown";
    private static final String TAG_VARIANT = "DisplayVariant";

    @Nullable
    private Identifier trophyId;
    private int cooldown;
    private int displayVariant;
    @Nullable
    private Entity renderEntity;

    public TrophyBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.TROPHY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TrophyBlockEntity trophy) {
        if (trophy.cooldown > 0) {
            trophy.cooldown--;
        }
        trophy.definition().ifPresent(def -> {
            TrophyBehavior behavior = OCRegistries.TROPHY_BEHAVIORS.get(def.behavior()).map(net.minecraft.core.Holder.Reference::value).orElse(null);
            if (behavior != null) {
                behavior.onTick(trophy, def);
            }
        });
    }

    public void onActivated(Player player) {
        Level level = this.level;
        if (level == null || level.isClientSide() || cooldown > 0) {
            return;
        }
        Optional<TrophyDefinition> definition = definition();
        if (definition.isEmpty()) {
            return;
        }
        TrophyDefinition def = definition.get();
        TrophyBehavior behavior = OCRegistries.TROPHY_BEHAVIORS.get(def.behavior()).map(net.minecraft.core.Holder.Reference::value).orElse(null);
        if (behavior == null) {
            return;
        }
        int next = behavior.onActivate(this, player, def);
        if (next < 0) {
            // Negative means the behavior did nothing (e.g. mooshroom with no space).
            return;
        }
        playMobSound(def);
        cooldown = next;
        setChanged();
        sync();
    }

    private void playMobSound(TrophyDefinition def) {
        Level level = this.level;
        if (level == null) {
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(def.entity());
        if (type == null) {
            return;
        }
        Entity entity = type.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        if (entity instanceof Slime || entity instanceof MagmaCube) {
            level.playSound(null, worldPosition, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.BLOCKS,
                    1.0F, 0.8F + level.getRandom().nextFloat() * 0.4F);
            if (entity != null) {
                entity.discard();
            }
            return;
        }
        if (entity instanceof Mob mob) {
            mob.setPos(worldPosition.getX() + 0.5D, worldPosition.getY(), worldPosition.getZ() + 0.5D);
            mob.playAmbientSound();
            entity.discard();
        } else if (entity != null) {
            entity.discard();
        }
    }

    public void loadFromItem(ItemStack stack) {
        Identifier id = stack.get(OCDataComponents.TROPHY_ID.get());
        if (id != null) {
            this.trophyId = id;
        }
        setChanged();
        sync();
    }

    public ItemStack asItem() {
        ItemStack stack = new ItemStack(getBlockState().getBlock());
        if (trophyId != null) {
            stack.set(OCDataComponents.TROPHY_ID.get(), trophyId);
        }
        return stack;
    }

    public Optional<TrophyDefinition> definition() {
        if (trophyId == null || level == null) {
            return Optional.empty();
        }
        return level.registryAccess().lookup(OCRegistries.TROPHY)
                .flatMap(reg -> reg.getOptional(trophyId));
    }

    @Nullable
    public Identifier getTrophyId() {
        return trophyId;
    }

    public void setTrophyId(@Nullable Identifier trophyId) {
        this.trophyId = trophyId;
        this.renderEntity = null;
        setChanged();
        sync();
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getDisplayVariant() {
        return displayVariant;
    }

    public void setDisplayVariant(int displayVariant) {
        this.displayVariant = Math.max(0, displayVariant);
        this.renderEntity = null;
        setChanged();
        sync();
    }

    @Nullable
    public Entity getOrCreateRenderEntity() {
        if (level == null || trophyId == null) {
            return null;
        }
        Optional<TrophyDefinition> definition = definition();
        if (definition.isEmpty()) {
            return null;
        }
        TrophyDefinition def = definition.get();
        if (renderEntity == null || renderEntity.isRemoved()) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(def.entity());
            if (type == null) {
                return null;
            }
            renderEntity = type.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (renderEntity != null) {
                renderEntity.setYRot(0.0F);
                renderEntity.yRotO = 0.0F;
                renderEntity.setXRot(0.0F);
                renderEntity.xRotO = 0.0F;
                if (renderEntity instanceof Bat bat) {
                    bat.setResting(true);
                }
                applyDisplayVariant(renderEntity);
            }
        }
        return renderEntity;
    }

    private void applyDisplayVariant(Entity entity) {
        Holder<VillagerProfession> profession = professionByIndex(displayVariant);
        Holder<VillagerType> plains = BuiltInRegistries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS);
        if (entity instanceof Villager villager) {
            VillagerData data = villager.getVillagerData();
            villager.setVillagerData(data.withProfession(profession).withType(plains));
        } else if (entity instanceof ZombieVillager zombie) {
            VillagerData data = zombie.getVillagerData();
            zombie.setVillagerData(data.withProfession(profession).withType(plains));
        }
    }

    private static Holder<VillagerProfession> professionByIndex(int index) {
        var list = BuiltInRegistries.VILLAGER_PROFESSION.listElements().toList();
        if (list.isEmpty()) {
            return BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE);
        }
        return list.get(Math.floorMod(index, list.size()));
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if (trophyId != null) {
            tag.putString(TAG_TROPHY, trophyId.toString());
        }
        tag.putInt(TAG_COOLDOWN, cooldown);
        tag.putInt(TAG_VARIANT, displayVariant);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        if (tag.keySet().contains(TAG_TROPHY)) {
            trophyId = Identifier.tryParse(tag.getStringOr(TAG_TROPHY, ""));
        } else {
            trophyId = null;
        }
        cooldown = tag.getIntOr(TAG_COOLDOWN, 0);
        displayVariant = tag.getIntOr(TAG_VARIANT, 0);
        renderEntity = null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static ResourceKey<TrophyDefinition> key(Identifier id) {
        return ResourceKey.create(OCRegistries.TROPHY, id);
    }

    public static Optional<Holder.Reference<TrophyDefinition>> holder(Level level, Identifier id) {
        return level.registryAccess().lookup(OCRegistries.TROPHY)
                .flatMap(reg -> reg.get(key(id)));
    }
}
