package dev.opencubes.content.tomfoolery;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.content.tomfoolery.effects.FlimFlamEffects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public final class FlimFlamRegistry {

    public static final FlimFlamRegistry INSTANCE = new FlimFlamRegistry();

    private final Map<String, FlimFlamDescription> byName = new LinkedHashMap<>();
    private final List<FlimFlamDescription> all = new ArrayList<>();
    private boolean registered;

    private FlimFlamRegistry() {}

    public void bootstrap() {
        if (registered) {
            return;
        }
        registered = true;
        FlimFlamEffects.registerAll(this);
    }

    public void register(FlimFlamDescription description) {
        byName.put(description.name(), description);
        all.add(description);
    }

    public List<FlimFlamDescription> all() {
        bootstrap();
        return Collections.unmodifiableList(all);
    }

    public List<String> names() {
        bootstrap();
        return List.copyOf(byName.keySet());
    }

    public FlimFlamDescription byName(String name) {
        bootstrap();
        return byName.get(name);
    }

    public boolean isBlocked(FlimFlamDescription effect) {
        if (OCCommonConfig.FLIM_FLAM_SAFE_ONLY.get() && !effect.safe()) {
            return true;
        }
        List<? extends String> list = OCCommonConfig.FLIM_FLAM_LIST.get();
        boolean onList = list.contains(effect.name());
        return OCCommonConfig.FLIM_FLAM_WHITELIST.get() ^ onList;
    }
}
