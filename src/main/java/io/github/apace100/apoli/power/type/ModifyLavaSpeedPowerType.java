package io.github.apace100.apoli.power.type;

import de.dafuqs.additionalentityattributes.AdditionalEntityAttributes;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.List;

public class ModifyLavaSpeedPowerType extends ConditionedAttributePowerType {

    public ModifyLavaSpeedPowerType(Power power, LivingEntity entity) {
        super(power, entity, 10,false);
    }

    public static PowerTypeFactory<ModifyLavaSpeedPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_lava_speed"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataTypes.ATTRIBUTE_MODIFIERS, null),
            data -> (power, LivingEntity) -> {

                ModifyLavaSpeedPowerType powerType = new ModifyLavaSpeedPowerType(power, LivingEntity);

                data.<EntityAttributeModifier>ifPresent("modifier", mod -> powerType.addModifier(AdditionalEntityAttributes.LAVA_SPEED, mod));
                data.<List<EntityAttributeModifier>>ifPresent("modifiers", mods -> mods.forEach(mod -> powerType.addModifier(AdditionalEntityAttributes.LAVA_SPEED, mod)));

                return powerType;

            }
        ).allowCondition();
    }

}
