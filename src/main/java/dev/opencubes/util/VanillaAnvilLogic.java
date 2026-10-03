package dev.opencubes.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;

/**
 * Runs vanilla {@link AnvilMenu#createResult()} against a fake player so Auto Anvil stays in
 * lockstep with anvil recipes, repair costs and NeoForge {@code AnvilUpdateEvent}.
 */
public final class VanillaAnvilLogic {

    private final ItemStack output;
    private final int levelCost;
    private final int materialCost;

    public VanillaAnvilLogic(ServerLevel level, ItemStack tool, ItemStack modifier) {
        FakePlayer player = OCFakePlayers.get(level);
        AnvilMenu menu = new AnvilMenu(0, player.getInventory(), ContainerLevelAccess.NULL);
        menu.setItem(AnvilMenu.INPUT_SLOT, menu.incrementStateId(), tool.copy());
        menu.setItem(AnvilMenu.ADDITIONAL_SLOT, menu.incrementStateId(), modifier.copy());
        this.output = menu.getSlot(AnvilMenu.RESULT_SLOT).getItem().copy();
        this.levelCost = menu.getCost();
        this.materialCost = menu.repairItemCountCost;
    }

    public ItemStack getOutputStack() {
        return output;
    }

    public int getLevelCost() {
        return levelCost;
    }

    /** How many items to consume from the modifier slot; zero means clear the whole stack. */
    public int getModifierCost() {
        return materialCost;
    }
}
