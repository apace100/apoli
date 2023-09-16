package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class ShaderPower extends Power implements Prioritized<ShaderPower> {

    private final Identifier shaderLocation;

    private final boolean toggleable;
    private final int priority;

    public ShaderPower(PowerType<?> type, LivingEntity entity, Identifier shaderLocation, boolean toggleable, int priority) {
        super(type, entity);
        this.shaderLocation = shaderLocation;
        this.toggleable = toggleable;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public Identifier getShaderLocation() {
        return shaderLocation;
    }

    public boolean isToggleable() {
        return toggleable;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("shader"),
            new SerializableData()
                .add("shader", SerializableDataTypes.IDENTIFIER)
                .add("toggleable", SerializableDataTypes.BOOLEAN, true)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (powerType, livingEntity) -> new ShaderPower(
                powerType,
                livingEntity,
                data.get("shader"),
                data.get("toggleable"),
                data.get("priority")
            )
        ).allowCondition();
    }
}
