package dev.opencubes.content.tomfoolery.effects;

import dev.opencubes.util.ServerLevels;

import dev.opencubes.content.tomfoolery.FlimFlamDescription;
import dev.opencubes.content.tomfoolery.FlimFlamRegistry;
import dev.opencubes.content.tomfoolery.LoreGenerator;
import dev.opencubes.registry.OCDataComponents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class FlimFlamEffects {

    private FlimFlamEffects() {}

    public static void registerAll(FlimFlamRegistry registry) {
        registry.register(FlimFlamDescription.create("inventory-shuffle", -50, 100, FlimFlamEffects::inventoryShuffle));
        registry.register(FlimFlamDescription.create("useless-tool", -125, 50, FlimFlamEffects::uselessTool));
        registry.register(FlimFlamDescription.create("bane", -125, 100, FlimFlamEffects::bane));
        registry.register(FlimFlamDescription.create("epic-lore", -10, 100, FlimFlamEffects::epicLore));
        registry.register(FlimFlamDescription.create("living-rename", -10, 100, FlimFlamEffects::livingRename));
        registry.register(FlimFlamDescription.create("squid", -75, 50, FlimFlamEffects::squid));
        registry.register(FlimFlamDescription.create("sheep-dye", -5, 50, FlimFlamEffects::sheepDye));
        registry.register(FlimFlamDescription.create("invisible-mobs", -25, 10, FlimFlamEffects::invisibleMobs));
        registry.register(FlimFlamDescription.create("sound", -5, 150, FlimFlamEffects::sound).markSilent());

        registry.register(FlimFlamDescription.create("snowballs", -50, 50, FlimFlamEffects::snowballs).markUnsafe());
        registry.register(FlimFlamDescription.create("teleport", -100, 30, FlimFlamEffects::teleport).markUnsafe());
        registry.register(FlimFlamDescription.create("mount", -150, 25, FlimFlamEffects::mount).markUnsafe());
        registry.register(FlimFlamDescription.create("encase", -50, 50, FlimFlamEffects::encase)
                .markUnsafe().setRange(Integer.MIN_VALUE, -300));
        registry.register(FlimFlamDescription.create("creepers", -60, 50, FlimFlamEffects::creepers).markUnsafe());
        registry.register(FlimFlamDescription.create("disarm", -50, 50, FlimFlamEffects::disarm).markUnsafe());
        registry.register(FlimFlamDescription.create("effect", -75, 75, FlimFlamEffects::effect).markUnsafe());
        registry.register(FlimFlamDescription.create("skyblock", -100, 150, FlimFlamEffects::skyblock)
                .markUnsafe().setRange(Integer.MIN_VALUE, -400));
    }

    private static boolean inventoryShuffle(ServerPlayer player) {
        if (player.containerMenu != player.inventoryMenu) {
            return false;
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            stacks.add(player.getInventory().getItem(i));
        }
        Collections.shuffle(stacks, new java.util.Random(player.getRandom().nextLong()));
        for (int i = 0; i < stacks.size(); i++) {
            player.getInventory().setItem(i, stacks.get(i));
        }
        player.getInventory().setChanged();
        return true;
    }

    private static boolean uselessTool(ServerPlayer player) {
        ItemStack[] options = {
                new ItemStack(Items.WOODEN_PICKAXE),
                new ItemStack(Items.WOODEN_AXE),
                new ItemStack(Items.WOODEN_SHOVEL),
                new ItemStack(Items.WOODEN_HOE),
                new ItemStack(Items.WOODEN_SWORD)
        };
        ItemStack tool = options[player.getRandom().nextInt(options.length)];
        tool.setDamageValue(tool.getMaxDamage() - 1);
        dropNear(player, tool);
        return true;
    }

    private static boolean bane(ServerPlayer player) {
        var holder = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .get(Enchantments.BANE_OF_ARTHROPODS);
        if (holder.isEmpty()) {
            return false;
        }
        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getCount() == 1 && !stack.isEnchanted() && stack.isEnchantable()) {
                stack.enchant(holder.get(), 5);
                return true;
            }
        }
        return false;
    }

    private static boolean epicLore(ServerPlayer player) {
        ItemStack target = firstWornOrHeld(player);
        if (target.isEmpty()) {
            return false;
        }
        String lore = LoreGenerator.generateLore(player.getGameProfile().name(),
                target.getHoverName().getString());
        target.set(OCDataComponents.EPIC_LORE.get(), lore);
        List<Component> lines = List.of(Component.literal(lore).withStyle(style -> style.withItalic(true).withColor(0x55FF55)));
        target.set(DataComponents.LORE, new ItemLore(lines));
        return true;
    }

    private static boolean livingRename(ServerPlayer player) {
        AABB box = player.getBoundingBox().inflate(8.0D);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && !e.hasCustomName());
        if (entities.isEmpty()) {
            return false;
        }
        LivingEntity pick = entities.get(player.getRandom().nextInt(entities.size()));
        pick.setCustomName(Component.literal(LoreGenerator.generateName()));
        pick.setCustomNameVisible(true);
        return true;
    }

    private static boolean squid(ServerPlayer player) {
        if (player.isPassenger()) {
            return false;
        }
        ServerLevel level = ServerLevels.of(player);
        Squid squid = EntityType.SQUID.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        if (squid == null) {
            return false;
        }
        squid.setPos(player.getX(), player.getY(), player.getZ());
        squid.setYRot(0.0F);
        squid.setXRot(0.0F);
        squid.setCustomName(Component.literal(LoreGenerator.generateName()));
        level.addFreshEntity(squid);
        return true;
    }

    private static boolean sheepDye(ServerPlayer player) {
        List<Sheep> sheep = player.level().getEntitiesOfClass(Sheep.class, player.getBoundingBox().inflate(10.0D));
        if (sheep.isEmpty()) {
            return false;
        }
        Sheep pick = sheep.get(player.getRandom().nextInt(sheep.size()));
        pick.setColor(DyeColor.byId(player.getRandom().nextInt(16)));
        return true;
    }

    private static boolean invisibleMobs(ServerPlayer player) {
        List<LivingEntity> mobs = player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(16.0D),
                e -> e != player && !(e instanceof ServerPlayer));
        boolean any = false;
        for (LivingEntity mob : mobs) {
            if (player.getRandom().nextFloat() < 0.3F) {
                mob.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 60 * 10, 0));
                any = true;
            }
        }
        return any;
    }

    private static boolean sound(ServerPlayer player) {
        player.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 1.0F);
        return true;
    }

    private static boolean snowballs(ServerPlayer player) {
        ServerLevel level = ServerLevels.of(player);
        for (int i = 0; i < 200; i++) {
            Snowball ball = new Snowball(level,
                    player.getX() + (player.getRandom().nextDouble() - 0.5D) * 4.0D,
                    player.getY() + 10.0D + player.getRandom().nextDouble() * 4.0D,
                    player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 4.0D,
                    new ItemStack(Items.SNOWBALL));
            ball.setOwner(player);
            ball.setDeltaMovement(0.0D, -0.2D, 0.0D);
            level.addFreshEntity(ball);
        }
        return true;
    }

    private static boolean teleport(ServerPlayer player) {
        ServerLevel level = ServerLevels.of(player);
        ThrownEnderpearl pearl = new ThrownEnderpearl(level, player, new ItemStack(Items.ENDER_PEARL));
        pearl.setPos(player.getX(), player.getEyeY(), player.getZ());
        float yaw = player.getRandom().nextFloat() * 360.0F;
        float pitch = -10.0F - player.getRandom().nextFloat() * 50.0F;
        float speed = 1.5F + player.getRandom().nextFloat();
        pearl.shootFromRotation(player, pitch, yaw, 0.0F, speed, 1.0F);
        level.addFreshEntity(pearl);
        return true;
    }

    private static boolean mount(ServerPlayer player) {
        List<LivingEntity> candidates = player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(6.0D),
                e -> e != player && !(e instanceof Creeper) && !(e instanceof Squid) && !e.isVehicle());
        if (candidates.isEmpty()) {
            return false;
        }
        LivingEntity mount = candidates.get(player.getRandom().nextInt(candidates.size()));
        return player.startRiding(mount, true, true);
    }

    private static boolean encase(ServerPlayer player) {
        ServerLevel level = ServerLevels.of(player);
        BlockPos origin = player.blockPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean shell = Math.abs(dx) == 1 || Math.abs(dz) == 1 || dy == 0 || dy == 2;
                    BlockPos pos = origin.offset(dx, dy, dz);
                    if (shell) {
                        if (level.getBlockState(pos).canBeReplaced()) {
                            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                        }
                    } else if (level.getBlockState(pos).canBeReplaced()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
        BlockPos torch = origin.above();
        if (level.getBlockState(torch).canBeReplaced()) {
            level.setBlock(torch, Blocks.TORCH.defaultBlockState(), 3);
        }
        return true;
    }

    private static boolean creepers(ServerPlayer player) {
        ServerLevel level = ServerLevels.of(player);
        for (int i = 0; i < 15; i++) {
            Creeper creeper = EntityType.CREEPER.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (creeper == null) {
                continue;
            }
            double angle = player.getRandom().nextDouble() * Math.PI * 2.0D;
            double dist = 2.0D + player.getRandom().nextDouble() * 3.0D;
            creeper.setPos(
                    player.getX() + Math.cos(angle) * dist,
                    player.getY(),
                    player.getZ() + Math.sin(angle) * dist);
            creeper.setYRot(player.getRandom().nextFloat() * 360.0F);
            creeper.setXRot(0.0F);
            // Harmless lookalike: no AI target, tiny explosion power via no ignition.
            creeper.setNoAi(true);
            creeper.addEffect(new MobEffectInstance(MobEffects.GLOWING, 20 * 20, 0));
            creeper.getPersistentData().putBoolean("opencubesDummyCreeper", true);
            level.addFreshEntity(creeper);
        }
        int when = level.getServer().getTickCount() + 400;
        level.getServer().schedule(new net.minecraft.server.TickTask(when, () -> {
            for (Creeper c : level.getEntitiesOfClass(Creeper.class, player.getBoundingBox().inflate(48.0D))) {
                if (c.getPersistentData().getBooleanOr("opencubesDummyCreeper", false)) {
                    c.discard();
                }
            }
        }));
        return true;
    }

    private static boolean disarm(ServerPlayer player) {
        boolean any = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR
                    && slot != EquipmentSlot.MAINHAND
                    && slot != EquipmentSlot.OFFHAND) {
                continue;
            }
            if (player.getRandom().nextFloat() < 0.4F) {
                ItemStack stack = player.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                    dropNear(player, stack);
                    any = true;
                }
            }
        }
        return any;
    }

    private static boolean effect(ServerPlayer player) {
        MobEffectInstance[] pool = {
                new MobEffectInstance(MobEffects.BLINDNESS, 20 * 15, 0),
                new MobEffectInstance(MobEffects.NAUSEA, 20 * 15, 0),
                new MobEffectInstance(MobEffects.MINING_FATIGUE, 20 * 30, 2),
                new MobEffectInstance(MobEffects.SLOWNESS, 20 * 20, 1),
                new MobEffectInstance(MobEffects.HUNGER, 20 * 30, 1),
                new MobEffectInstance(MobEffects.WEAKNESS, 20 * 20, 1)
        };
        player.addEffect(pool[player.getRandom().nextInt(pool.length)]);
        player.addEffect(pool[player.getRandom().nextInt(pool.length)]);
        return true;
    }

    private static boolean skyblock(ServerPlayer player) {
        if (player.level().dimension() == Level.NETHER) {
            return false;
        }
        ServerLevel level = ServerLevels.of(player);
        BlockPos dest = player.blockPosition().above(150);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(dest.offset(dx, -1, dz), Blocks.ICE.defaultBlockState(), 3);
            }
        }
        player.teleportTo(dest.getX() + 0.5D, dest.getY(), dest.getZ() + 0.5D);
        return true;
    }

    private static ItemStack firstWornOrHeld(ServerPlayer player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void dropNear(ServerPlayer player, ItemStack stack) {
        ItemEntity entity = new ItemEntity(player.level(), player.getX(), player.getY() + 1.0D, player.getZ(), stack);
        entity.setPickUpDelay(40);
        entity.setDeltaMovement(new Vec3(
                (player.getRandom().nextDouble() - 0.5D) * 0.2D,
                0.2D,
                (player.getRandom().nextDouble() - 0.5D) * 0.2D));
        player.level().addFreshEntity(entity);
    }
}
