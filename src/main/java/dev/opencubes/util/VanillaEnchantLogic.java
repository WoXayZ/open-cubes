package dev.opencubes.util;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * Seeded enchanting-table slot math for Auto Enchanting Table. Calls into
 * {@link EnchantmentHelper} so treasure tags and NeoForge item hooks stay consistent.
 */
public final class VanillaEnchantLogic {

    public enum Level {
        L1, L2, L3
    }

    private final long seed;
    private final RandomSource random = RandomSource.create();

    private ItemStack toEnchant = ItemStack.EMPTY;
    private Level level = Level.L1;
    private int xpLevels;

    public VanillaEnchantLogic(long seed) {
        this.seed = seed;
    }

    public boolean setup(ItemStack stack, Level level, int power) {
        if (stack.isEmpty() || !stack.isEnchantable()) {
            return false;
        }
        random.setSeed(seed);
        this.toEnchant = stack.copy();
        this.level = level;
        this.xpLevels = EnchantmentHelper.getEnchantmentCost(random, level.ordinal(), power, toEnchant);
        // Match vanilla EnchantmentMenu: zero only when strictly below the slot's minimum.
        if (this.xpLevels < level.ordinal() + 1) {
            this.xpLevels = 0;
        }
        return true;
    }

    /** Player levels deducted when taking this slot (1 / 2 / 3). */
    public int getLevelCost() {
        return level.ordinal() + 1;
    }

    /** Minimum player level (from tank XP) required to see this option. */
    public int getLevelRequirement() {
        return xpLevels;
    }

    public int getLapisCost() {
        return level.ordinal() + 1;
    }

    public ItemStack enchant(RegistryAccess registries) {
        if (toEnchant.isEmpty() || xpLevels <= 0) {
            return ItemStack.EMPTY;
        }

        random.setSeed(seed + level.ordinal());
        Optional<HolderSet.Named<Enchantment>> tag = registries.lookupOrThrow(Registries.ENCHANTMENT)
                .get(EnchantmentTags.IN_ENCHANTING_TABLE);
        if (tag.isEmpty()) {
            return ItemStack.EMPTY;
        }

        List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(
                random, toEnchant, xpLevels, tag.get().stream());
        if (toEnchant.is(net.minecraft.world.item.Items.BOOK) && list.size() > 1) {
            list.remove(random.nextInt(list.size()));
        }
        if (list.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return toEnchant.getItem().applyEnchantments(toEnchant, list);
    }
}
