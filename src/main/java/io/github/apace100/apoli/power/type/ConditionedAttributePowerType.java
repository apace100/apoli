package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

import java.util.List;

public class ConditionedAttributePowerType extends AttributePowerType {

    private final int tickRate;

    public ConditionedAttributePowerType(Power power, LivingEntity entity, int tickRate, boolean updateHealth) {
        super(power, entity, updateHealth);
        this.tickRate = tickRate;
        this.setTicking(true);
    }

    @Override
    public void onAdded() {

    }

    @Override
    public void onRemoved() {

    }

    @Override
    public void onLost() {
        this.removeTempMods();
    }

    @Override
    public void tick() {

        if (entity.age % tickRate != 0) {
            return;
        }

        if (this.isActive()) {
            this.applyTempMods();
        }

        else {
            this.removeTempMods();
        }

    }

    public static PowerTypeFactory<ConditionedAttributePowerType> getConditionedFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("conditioned_attribute"),
            new SerializableData()
                .add("modifier", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIER, null)
                .add("modifiers", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIERS, null)
                .add("tick_rate", SerializableDataTypes.POSITIVE_INT, 20)
                .add("update_health", SerializableDataTypes.BOOLEAN, true),
            data -> (power, entity) -> {

                ConditionedAttributePowerType conditionedAttributePower = new ConditionedAttributePowerType(
                    power,
                    entity,
                    data.get("tick_rate"),
                    data.get("update_health")
                );

                data.<AttributedEntityAttributeModifier>ifPresent("modifier", conditionedAttributePower::addModifier);
                data.<List<AttributedEntityAttributeModifier>>ifPresent("modifiers", mods -> mods.forEach(conditionedAttributePower::addModifier));

                return conditionedAttributePower;

            }
        ).allowCondition();
    }

}
