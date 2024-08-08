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

public class ModifySwimSpeedPowerType extends ConditionedAttributePowerType {

    public ModifySwimSpeedPowerType(Power power, LivingEntity entity) {
        super(power, entity, 10,false);
    }

    public static PowerTypeFactory<ModifySwimSpeedPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_swim_speed"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataTypes.ATTRIBUTE_MODIFIERS, null),
            data -> (power, entity) -> {

                ModifySwimSpeedPowerType modifySwimSpeedPower = new ModifySwimSpeedPowerType(power, entity);

                data.<EntityAttributeModifier>ifPresent("modifier", mod -> modifySwimSpeedPower.addModifier(AdditionalEntityAttributes.WATER_SPEED, mod));
                data.<List<EntityAttributeModifier>>ifPresent("modifiers", mods -> mods.forEach(mod -> modifySwimSpeedPower.addModifier(AdditionalEntityAttributes.WATER_SPEED, mod)));

                return modifySwimSpeedPower;

            }
        ).allowCondition();
    }

}
