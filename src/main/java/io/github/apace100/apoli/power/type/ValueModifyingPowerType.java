package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public class ValueModifyingPowerType extends PowerType {

    private final List<Modifier> modifiers = new LinkedList<>();

    public ValueModifyingPowerType(Power power, LivingEntity entity) {
        super(power, entity);
    }

    public void addModifier(Modifier modifier) {
        this.modifiers.add(modifier);
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public static <T extends ValueModifyingPowerType> PowerTypeFactory<T> createValueModifyingFactory(Identifier id, BiFunction<Power, LivingEntity, T> powerConstructor) {
        return new PowerTypeFactory<>(id,
            new SerializableData()
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data -> (power, entity) -> {

                T powerType = powerConstructor.apply(power, entity);

                data.ifPresent("modifier", powerType::addModifier);
                data.<List<Modifier>>ifPresent("modifiers", mods -> mods.forEach(powerType::addModifier));

                return powerType;

            }
        ).allowCondition();
    }

}
