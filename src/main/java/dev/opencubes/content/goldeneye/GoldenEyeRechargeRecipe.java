package dev.opencubes.content.goldeneye;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Shapeless: golden eye + N ender pearls → eye with damage reduced by 10N. */
public class GoldenEyeRechargeRecipe extends CustomRecipe {

    public static final GoldenEyeRechargeRecipe INSTANCE = new GoldenEyeRechargeRecipe();
    public static final MapCodec<GoldenEyeRechargeRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC)
                    .forGetter(recipe -> CraftingBookCategory.MISC)
    ).apply(instance, category -> INSTANCE));
    public static final StreamCodec<RegistryFriendlyByteBuf, GoldenEyeRechargeRecipe> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<GoldenEyeRechargeRecipe> SERIALIZER =
            new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private GoldenEyeRechargeRecipe() {}

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack eye = ItemStack.EMPTY;
        int pearls = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(OCItems.GOLDEN_EYE.get())) {
                if (!eye.isEmpty()) {
                    return false;
                }
                eye = stack;
            } else if (stack.is(Items.ENDER_PEARL)) {
                pearls += stack.getCount();
            } else {
                return false;
            }
        }
        return !eye.isEmpty() && pearls > 0 && eye.isDamaged();
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack eye = ItemStack.EMPTY;
        int pearls = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(OCItems.GOLDEN_EYE.get())) {
                eye = stack.copy();
            } else if (stack.is(Items.ENDER_PEARL)) {
                pearls += stack.getCount();
            }
        }
        if (eye.isEmpty()) {
            return ItemStack.EMPTY;
        }
        eye.setCount(1);
        eye.setDamageValue(Math.max(0, eye.getDamageValue()
                - pearls * OCCommonConfig.GOLDEN_EYE_PEARL_REPAIR.get()));
        return eye;
    }

    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }
}
