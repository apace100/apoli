package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierOperation;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class ModifyFallingPowerType extends ValueModifyingPowerType {

    private final boolean takeFallDamage;

    public ModifyFallingPowerType(Power power, LivingEntity entity, @Nullable Double velocity, boolean takeFallDamage, Modifier modifier, List<Modifier> modifiers) {
        super(power, entity);
        this.takeFallDamage = takeFallDamage;

        if (velocity != null) {
            this.addModifier(ModifierUtil.createSimpleModifier(ModifierOperation.SET_TOTAL, velocity));
        }

        else {

            if (modifier != null) {
                this.addModifier(modifier);
            }

            if (modifiers != null) {
                modifiers.forEach(this::addModifier);
            }

        }

    }

    public boolean shouldTakeFallDamage() {
        return takeFallDamage;
    }

    public static boolean shouldNegateFallDamage(Entity entity) {
        return PowerHolderComponent.hasPowerType(entity, ModifyFallingPowerType.class, Predicate.not(ModifyFallingPowerType::shouldTakeFallDamage));
    }

    public static PowerTypeFactory<ModifyFallingPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_falling"),
            new SerializableData()
                .add("velocity", SerializableDataTypes.DOUBLE, null)
                .add("take_fall_damage", SerializableDataTypes.BOOLEAN, true)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data -> (power, entity) -> new ModifyFallingPowerType(power, entity,
                data.get("velocity"),
                data.get("take_fall_damage"),
                data.get("modifier"),
                data.get("modifiers")
            )
        ).allowCondition();
    }

}
