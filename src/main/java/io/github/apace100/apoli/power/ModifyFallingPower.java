package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierOperation;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

import java.util.List;

public class ModifyFallingPower extends ValueModifyingPower {

    public final boolean takeFallDamage;

    public ModifyFallingPower(PowerType type, LivingEntity entity, double velocity, boolean takeFallDamage) {
        super(type, entity);
        this.takeFallDamage = takeFallDamage;
        this.addModifier(ModifierUtil.createSimpleModifier(ModifierOperation.SET_TOTAL, velocity));
    }

    public ModifyFallingPower(PowerType type, LivingEntity entity, boolean takeFallDamage) {
        super(type, entity);
        this.takeFallDamage = takeFallDamage;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_falling"),
            new SerializableData()
                .add("velocity", SerializableDataTypes.DOUBLE, null)
                .add("take_fall_damage", SerializableDataTypes.BOOLEAN, true)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data ->
                (type, player) -> {
                    ModifyFallingPower power;
                    if(data.isPresent("velocity")) {
                        power = new ModifyFallingPower(type, player,
                            data.getDouble("velocity"),
                            data.getBoolean("take_fall_damage"));
                    } else {
                        power = new ModifyFallingPower(type, player,
                            data.getBoolean("take_fall_damage"));
                    }
                    ModifyFallingPower finalPower = power;
                    data.ifPresent("modifier", finalPower::addModifier);
                    data.<List<Modifier>>ifPresent("modifiers",
                        mods -> mods.forEach(finalPower::addModifier)
                    );
                    return finalPower;
                })
            .allowCondition();
    }
}
