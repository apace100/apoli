package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class ShaderPowerType extends PowerType implements Prioritized<ShaderPowerType> {

    private final Identifier shaderLocation;

    private final boolean toggleable;
    private final int priority;

    public ShaderPowerType(Power power, LivingEntity entity, Identifier shaderLocation, boolean toggleable, int priority) {
        super(power, entity);
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

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("shader"),
            new SerializableData()
                .add("shader", SerializableDataTypes.IDENTIFIER)
                .add("toggleable", SerializableDataTypes.BOOLEAN, true)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new ShaderPowerType(power, entity,
                data.get("shader"),
                data.get("toggleable"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
