package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class ShaderPower extends Power {

    private final Identifier shaderLocation;
    private final boolean toggleable;

    public ShaderPower(PowerType<?> type, LivingEntity entity, Identifier shaderLocation, boolean toggleable) {
        super(type, entity);
        this.shaderLocation = shaderLocation;
        this.toggleable = toggleable;
    }

    public Identifier getShaderLocation() {
        return shaderLocation;
    }

    public boolean isToggleable() {
        return toggleable;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("shader"),
            new SerializableData()
                .add("shader", SerializableDataTypes.IDENTIFIER)
                .add("toggleable", SerializableDataTypes.BOOLEAN, true),
            data ->
                (type, player) -> new ShaderPower(type, player, data.getId("shader"), data.getBoolean("toggleable")))
            .allowCondition();
    }
}
