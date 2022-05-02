package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;

import java.util.EnumSet;
import java.util.List;

public class ModifyVelocityPower extends ValueModifyingPower {

    public final EnumSet<Direction.Axis> axes;

    public ModifyVelocityPower(PowerType<?> type, LivingEntity entity, EnumSet<Direction.Axis> axes) {
        super(type, entity);
        this.axes = axes;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_velocity"),
            new SerializableData()
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null)
                .add("axes", SerializableDataTypes.AXIS_SET, EnumSet.allOf(Direction.Axis.class)),
            data ->
                (type, player) -> {
                    ModifyVelocityPower power = new ModifyVelocityPower(type, player, data.get("axes"));
                    data.ifPresent("modifier", power::addModifier);
                    data.<List<Modifier>>ifPresent("modifiers",
                        mods -> mods.forEach(power::addModifier)
                    );
                    return power;
                })
            .allowCondition();
    }
}