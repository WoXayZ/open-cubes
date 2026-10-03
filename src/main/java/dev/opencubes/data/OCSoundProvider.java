package dev.opencubes.data;

import dev.opencubes.OCConstants;
import dev.opencubes.registry.OCSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class OCSoundProvider extends SoundDefinitionsProvider {

    public OCSoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, OCConstants.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {
        add(OCSounds.ELEVATOR_ACTIVATE, SoundDefinition.definition()
                .subtitle("subtitles.opencubes.block.elevator.activate")
                .with(SoundDefinition.Sound.sound(OCConstants.id("elevator_activate"), SoundDefinition.SoundType.SOUND)));

        add(OCSounds.BEAR_TRAP_OPEN, SoundDefinition.definition()
                .subtitle("subtitles.opencubes.block.bear_trap.open")
                .with(SoundDefinition.Sound.sound(OCConstants.id("bear_trap_open"), SoundDefinition.SoundType.SOUND)));

        add(OCSounds.BEAR_TRAP_CLOSE, SoundDefinition.definition()
                .subtitle("subtitles.opencubes.block.bear_trap.close")
                .with(SoundDefinition.Sound.sound(OCConstants.id("bear_trap_close"), SoundDefinition.SoundType.SOUND))
                .with(SoundDefinition.Sound.sound(OCConstants.id("bear_trap_close_b"), SoundDefinition.SoundType.SOUND)));

        add(OCSounds.BOTTLER_DONE, SoundDefinition.definition()
                .subtitle("subtitles.opencubes.block.bottler.done")
                .with(SoundDefinition.Sound.sound(OCConstants.id("bottler_done"), SoundDefinition.SoundType.SOUND)));

        add(OCSounds.GRAVE_ROB, SoundDefinition.definition()
                .subtitle("subtitles.opencubes.block.grave.rob")
                .with(SoundDefinition.Sound.sound(OCConstants.id("grave_rob"), SoundDefinition.SoundType.SOUND)));

        add(OCSounds.SLIMALYZER_PING, SoundDefinition.definition()
                .subtitle("subtitles.opencubes.item.slimalyzer.ping")
                .with(SoundDefinition.Sound.sound(OCConstants.id("beep"), SoundDefinition.SoundType.SOUND)));

        add(OCSounds.PEDOMETER_USE, SoundDefinition.definition()
                .subtitle("subtitles.opencubes.item.pedometer.use")
                .with(SoundDefinition.Sound.sound(OCConstants.id("beep"), SoundDefinition.SoundType.SOUND)));
    }
}
