package io.github.apace100.apoli.power;

import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import java.util.*;

public interface Active {

    void onUse();
    void setKey(Key key);

    Key getKey();

    @Environment(EnvType.CLIENT)
    static void integrateCallback(MinecraftClient client) {

        if (client.player == null) {
            return;
        }

        List<Power> powers = PowerHolderComponent.KEY.get(client.player).getPowers();
        List<Power> triggeredPowers = new LinkedList<>();

        Map<String, Boolean> currentKeybindingStates = new HashMap<>();
        for (Power power : powers) {

            if (!(power instanceof Active activePower)) {
                continue;
            }

            Key key = activePower.getKey();
            KeyBinding keyBinding = ApoliClient.getKeyBinding(key.key);

            if (keyBinding == null) {
                continue;
            }

            if (currentKeybindingStates.computeIfAbsent(key.key, k -> keyBinding.isPressed()) && (key.continuous || !ApoliClient.lastKeyBindingStates.getOrDefault(key.key, false))) {
                triggeredPowers.add(power);
            }

        }

        ApoliClient.lastKeyBindingStates.putAll(currentKeybindingStates);
        if (!triggeredPowers.isEmpty()) {
            ApoliClient.performActivePowers(triggeredPowers);
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
