package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;

import java.util.EnumSet;
import java.util.List;

public class ModifyVelocityPowerType extends ValueModifyingPowerType {

    private final EnumSet<Direction.Axis> axes;

    public ModifyVelocityPowerType(Power power, LivingEntity entity, EnumSet<Direction.Axis> axes, Modifier modifier, List<Modifier> modifiers) {
        super(power, entity);
        this.axes = axes;

        if (modifier != null) {
            this.addModifier(modifier);
        }

        if (modifiers != null) {
            modifiers.forEach(this::addModifier);
        }

    }

    public boolean doesApply(Direction.Axis axis) {
        return axes.contains(axis);
    }

    public static PowerTypeFactory<ModifyVelocityPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_velocity"),
            new SerializableData()
                .add("axes", SerializableDataTypes.AXIS_SET, EnumSet.allOf(Direction.Axis.class))
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data -> (power, entity) -> new ModifyVelocityPowerType(power, entity,
                data.get("axes"),
                data.get("modifier"),
                data.get("modifiers")
            )
        ).allowCondition();
    }

}
