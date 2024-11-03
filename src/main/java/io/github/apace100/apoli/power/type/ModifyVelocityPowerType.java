package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class ModifyVelocityPowerType extends ValueModifyingPowerType {

    public static final TypedDataObjectFactory<ModifyVelocityPowerType> DATA_FACTORY = createConditionedModifyingDataFactory(
        new SerializableData()
            .add("axes", SerializableDataTypes.AXIS_SET, EnumSet.allOf(Direction.Axis.class)),
        (data, modifiers, condition) -> new ModifyVelocityPowerType(
            data.get("axes"),
            modifiers,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("axes", powerType.axes)
    );

    private final EnumSet<Direction.Axis> axes;

    public ModifyVelocityPowerType(EnumSet<Direction.Axis> axes, List<Modifier> modifiers, Optional<EntityCondition> condition) {
        super(modifiers, condition);
        this.axes = axes;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_VELOCITY;
    }

    public boolean doesApply(Direction.Axis axis) {
        return axes.contains(axis);
    }

}
