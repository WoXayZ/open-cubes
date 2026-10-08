package dev.opencubes.compat.jade;

import dev.opencubes.content.luggage.LuggageEntity;
import dev.opencubes.content.tank.TankBlock;
import dev.opencubes.content.tank.TankBlockEntity;
import dev.opencubes.content.trophy.TrophyBlock;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.view.HideThingsExtensionProvider;

@WailaPlugin
public final class OCJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(TrophyJadeProvider.Data.INSTANCE, TrophyBlockEntity.class);
        registration.registerBlockDataProvider(TankJadeProvider.Data.INSTANCE, TankBlockEntity.class);
        registration.registerEntityDataProvider(LuggageJadeProvider.Data.INSTANCE, LuggageEntity.class);
        registration.registerFluidStorage(HideThingsExtensionProvider.instance(), TankBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(TrophyJadeProvider.INSTANCE, TrophyBlock.class);
        registration.registerBlockComponent(TankJadeProvider.INSTANCE, TankBlock.class);
        registration.registerEntityComponent(LuggageJadeProvider.INSTANCE, LuggageEntity.class);
        registration.registerFluidStorageClient(HideThingsExtensionProvider.instance());
    }
}
