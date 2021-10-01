package io.github.apace100.apoli.util;

import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "power_config")
public class ApoliConfigClient extends ApoliConfig {

    @ConfigEntry.Gui.CollapsibleObject
    public ResourcesAndCooldowns resourcesAndCooldowns = new ResourcesAndCooldowns();

    public static class ResourcesAndCooldowns {

        public int hudOffsetX = 0;
        public int hudOffsetY = 0;
    }
}
