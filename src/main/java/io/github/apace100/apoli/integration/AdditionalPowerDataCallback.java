package io.github.apace100.apoli.integration;

import com.google.gson.JsonElement;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.util.Identifier;

/**
 * Use this callback by registering an additional data field with `PowerTypes.registerAdditionalData(...)`.
 */
public interface AdditionalPowerDataCallback {

    void readAdditionalPowerData(Identifier powerId, Identifier factoryId, boolean isSubPower, JsonElement data, PowerType powerType);
}
