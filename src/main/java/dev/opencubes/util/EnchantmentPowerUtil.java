package dev.opencubes.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;

/**
 * Bookshelf power around a position, matching vanilla enchanting-table geometry
 * ({@link EnchantingTableBlock#BOOKSHELF_OFFSETS}).
 */
public final class EnchantmentPowerUtil {

    private EnchantmentPowerUtil() {}

    public static float getPower(Level level, BlockPos pos) {
        float power = 0.0F;
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (EnchantingTableBlock.isValidBookShelf(level, pos, offset)) {
                BlockPos shelf = pos.offset(offset);
                power += level.getBlockState(shelf).getEnchantPowerBonus(level, shelf);
            }
        }
        return power;
    }
}
