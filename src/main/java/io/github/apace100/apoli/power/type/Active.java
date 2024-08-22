package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import java.util.*;

public interface Active {

    void onUse();

    Key getKey();

    default void setKey(Key key) {

    }

    @Environment(EnvType.CLIENT)
    static void integrateCallback(MinecraftClient client) {

        if (client.player == null) {
            return;
        }

        List<PowerType> powerTypes = PowerHolderComponent.KEY.get(client.player).getPowerTypes();
        List<PowerType> triggeredPowerTypes = new LinkedList<>();

        Map<String, Boolean> currentKeybindingStates = new HashMap<>();
        for (PowerType powerType : powerTypes) {

            if (!(powerType instanceof Active activePower)) {
                continue;
            }

            Key key = activePower.getKey();
            KeyBinding keyBinding = ApoliClient.getKeyBinding(key.key);

            if (keyBinding == null) {
                continue;
            }

            if (currentKeybindingStates.computeIfAbsent(key.key, k -> keyBinding.isPressed()) && (key.continuous || !ApoliClient.lastKeyBindingStates.getOrDefault(key.key, false))) {
                triggeredPowerTypes.add(powerType);
            }

        }

        ApoliClient.lastKeyBindingStates.putAll(currentKeybindingStates);
        if (!triggeredPowerTypes.isEmpty()) {
            ApoliClient.performActivePowers(triggeredPowerTypes);
        }

    }

    class Key {

        public String key = "none";
        public boolean continuous = false;

        @Override
        public boolean equals(final Object obj) {
            if (obj == this)
                return true;

            if (!(obj instanceof Active.Key otherKey))
                return false;

            return otherKey.key.equals(this.key) && otherKey.continuous == this.continuous;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.key, this.continuous);
        }
    }

}
