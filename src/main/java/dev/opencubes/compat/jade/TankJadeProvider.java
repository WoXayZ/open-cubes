package dev.opencubes.compat.jade;

import dev.opencubes.OCConstants;
import dev.opencubes.content.tank.TankBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;

public enum TankJadeProvider implements IBlockComponentProvider {
    INSTANCE;

    public static final Identifier UID = OCConstants.id("tank");

    private static final String TAG_AMOUNT = "Amount";
    private static final String TAG_CAPACITY = "Capacity";
    private static final String TAG_COUNT = "Count";
    private static final String TAG_FLUID = "Fluid";

    public enum Data implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof TankBlockEntity tank)) {
                return;
            }
            TankBlockEntity.NetworkContents network = tank.networkContents();
            data.putInt(TAG_AMOUNT, network.fluid().getAmount());
            data.putInt(TAG_CAPACITY, network.capacityMb());
            data.putInt(TAG_COUNT, network.tankCount());
            if (!network.fluid().isEmpty()) {
                data.putString(TAG_FLUID, BuiltInRegistries.FLUID.getKey(network.fluid().getFluid()).toString());
            }
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        // Drop Jade's per-block fluid bar so only the network total remains.
        tooltip.remove(JadeIds.UNIVERSAL_FLUID_STORAGE);

        CompoundTag data = accessor.getServerData();
        if (!data.contains(TAG_CAPACITY)) {
            return;
        }
        int capacityMb = data.getIntOr(TAG_CAPACITY, 0);
        int amountMb = data.getIntOr(TAG_AMOUNT, 0);
        int count = data.getIntOr(TAG_COUNT, 0);
        String capacityB = formatBuckets(capacityMb);
        String amountB = formatBuckets(amountMb);

        if (amountMb <= 0 || !data.contains(TAG_FLUID)) {
            tooltip.add(Component.translatable("opencubes.jade.tank.empty", capacityB));
        } else {
            Identifier fluidId = Identifier.tryParse(data.getStringOr(TAG_FLUID, ""));
            Fluid fluid = fluidId == null ? null : BuiltInRegistries.FLUID.get(fluidId).map(net.minecraft.core.Holder.Reference::value).orElse(null);
            Component name = fluid != null
                    ? new FluidStack(fluid, Math.max(1, amountMb)).getHoverName()
                    : Component.literal(data.getStringOr(TAG_FLUID, ""));
            tooltip.add(Component.translatable("opencubes.jade.tank.fluid", name, amountB, capacityB));
        }
        if (count > 1) {
            tooltip.add(Component.translatable("opencubes.jade.tank.count", count));
        }
    }

    private static String formatBuckets(int millibuckets) {
        double buckets = millibuckets / 1000.0D;
        if (Math.abs(buckets - Math.rint(buckets)) < 0.001D) {
            return String.format("%.0f", buckets);
        }
        return String.format("%.2f", buckets);
    }

    @Override
    public int getDefaultPriority() {
        // After FluidStorageProvider (1000) so remove() can see the default bar.
        return 1100;
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
