package dev.opencubes.compat.jei;

import dev.opencubes.OCConstants;
import dev.opencubes.content.trophy.TrophyBlockItem;
import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCItems;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@JeiPlugin
public final class OCJeiPlugin implements IModPlugin {

    private static final ResourceLocation UID = OCConstants.id("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(OCItems.TROPHY.get(), new ISubtypeInterpreter<ItemStack>() {
            @Override
            public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
                return ingredient.get(OCDataComponents.TROPHY_ID.get());
            }

            @Override
            @SuppressWarnings("deprecation")
            public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
                ResourceLocation id = ingredient.get(OCDataComponents.TROPHY_ID.get());
                return id == null ? "" : id.toString();
            }
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(OCBlocks.AUTO_ANVIL.get()), RecipeTypes.ANVIL);
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        List<ItemStack> stacks = new ArrayList<>();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            TrophyBlockItem.fillCreative(minecraft.level.registryAccess(), stacks::add);
        } else if (minecraft.getConnection() != null) {
            TrophyBlockItem.fillCreative(minecraft.getConnection().registryAccess(), stacks::add);
        } else {
            TrophyBlockItem.fillCreative(net.minecraft.core.RegistryAccess.EMPTY, stacks::add);
        }
        if (!stacks.isEmpty()) {
            registration.addExtraIngredients(VanillaTypes.ITEM_STACK, stacks);
        }
    }
}
