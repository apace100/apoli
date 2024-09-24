package io.github.apace100.apoli.power.type;

import de.dafuqs.additionalentityattributes.AdditionalEntityAttributes;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;

public class LavaVisionPowerType extends AttributePowerType {

    public LavaVisionPowerType(Power power, LivingEntity entity, float v) {
        super(power, entity, false);
        this.addModifier(AdditionalEntityAttributes.LAVA_VISIBILITY, new EntityAttributeModifier(this.getPowerId(), v - 1, EntityAttributeModifier.Operation.ADD_VALUE));
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("lava_vision"),
            new SerializableData()
                .add("v", SerializableDataTypes.FLOAT),
            data -> (power, entity) -> new LavaVisionPowerType(power, entity,
                data.getFloat("v")
            )
        ).allowCondition();
    }

}
