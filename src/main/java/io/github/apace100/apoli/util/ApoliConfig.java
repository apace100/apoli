package io.github.apace100.apoli.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class ApoliConfig implements ConfigData {

    @ConfigEntry.Gui.CollapsibleObject
    public ExecuteCommand executeCommand = new ExecuteCommand();

    @ConfigEntry.Gui.CollapsibleObject
    public ModifyPlayerSpawnPower modifyPlayerSpawnPower = new ModifyPlayerSpawnPower();

    public static class ExecuteCommand {
        @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
        public int permissionLevel = 2;
        public boolean showOutput = false;
    }

    public static class ModifyPlayerSpawnPower {
        public int radius = 6400;
        public int horizontalBlockCheckInterval = 64;
        public int verticalBlockCheckInterval = 64;
    }
}
