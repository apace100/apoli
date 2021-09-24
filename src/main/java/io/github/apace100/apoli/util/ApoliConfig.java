package io.github.apace100.apoli.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class ApoliConfig implements ConfigData {

    @ConfigEntry.Gui.CollapsibleObject
    public ExecuteCommand executeCommand = new ExecuteCommand();

    public static class ExecuteCommand {
        @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
        public int permissionLevel = 2;
        public boolean showOutput = false;
    }
}
