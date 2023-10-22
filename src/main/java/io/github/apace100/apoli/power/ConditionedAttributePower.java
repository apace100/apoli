package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

import java.util.List;

public class ConditionedAttributePower extends AttributePower {

    private final int tickRate;

    public ConditionedAttributePower(PowerType<?> type, LivingEntity entity, int tickRate, boolean updateHealth) {
        super(type, entity, updateHealth);
        this.setTicking(true);
        this.tickRate = tickRate;
    }

    @Override
    public void tick() {

        if (entity.age % tickRate != 0) {
            return;
        }

        if (this.isActive()) {
            this.applyTempMods();
        } else {
            this.removeTempMods();
        }

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("conditioned_attribute"),
            new SerializableData()
                .add("modifier", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIER, null)
                .add("modifiers", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIERS, null)
                .add("tick_rate", SerializableDataTypes.INT, 20)
                .add("update_health", SerializableDataTypes.BOOLEAN, true),
            data -> (powerType, livingEntity) -> {

                ConditionedAttributePower conditionedAttributePower = new ConditionedAttributePower(
                    powerType,
                    livingEntity,
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
