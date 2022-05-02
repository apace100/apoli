package io.github.apace100.apoli.power;

import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

public class ValueModifyingPower extends Power {

    private final List<Modifier> modifiers = new LinkedList<>();

    public ValueModifyingPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public void addModifier(Modifier modifier) {
        this.modifiers.add(modifier);
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public static PowerFactory createValueModifyingFactory(BiFunction<PowerType, LivingEntity, ValueModifyingPower> powerConstructor, Identifier identifier) {
        return new PowerFactory<>(identifier,
            new SerializableData()
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data ->
                (type, player) -> {
                    ValueModifyingPower power = powerConstructor.apply(type, player);
                    data.ifPresent("modifier", power::addModifier);
                    data.<List<Modifier>>ifPresent("modifiers",
                        mods -> mods.forEach(power::addModifier)
                    );
                    return power;
                })
            .allowCondition();
    }
}
