package dev.opencubes.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.opencubes.config.OCCommonConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class AddItemLootModifier extends LootModifier {

    public static final MapCodec<AddItemLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).and(instance.group(
                    ItemStack.CODEC.fieldOf("item").forGetter(m -> m.item),
                    Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance),
                    Codec.BOOL.optionalFieldOf("respect_config", true).forGetter(m -> m.respectConfig)
            )).apply(instance, AddItemLootModifier::new));

    private final ItemStack item;
    private final float chance;
    private final boolean respectConfig;

    public AddItemLootModifier(LootItemCondition[] conditions, int priority, ItemStack item, float chance, boolean respectConfig) {
        super(conditions, priority);
        this.item = item;
        this.chance = chance;
        this.respectConfig = respectConfig;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (respectConfig && !OCCommonConfig.TECHNICOLOR_GLASSES_LOOT.get()) {
            return generatedLoot;
        }
        if (context.getRandom().nextFloat() < chance) {
            generatedLoot.add(item.copy());
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
