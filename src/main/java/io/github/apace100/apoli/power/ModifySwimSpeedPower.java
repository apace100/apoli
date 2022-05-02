package io.github.apace100.apoli.power;

import de.dafuqs.additionalentityattributes.AdditionalEntityAttributes;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.List;

public class ModifySwimSpeedPower extends AttributePower {
    public ModifySwimSpeedPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity, false);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_swim_speed"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataTypes.ATTRIBUTE_MODIFIERS, null),
            data ->
                (type, player) -> {
                    ModifySwimSpeedPower power = new ModifySwimSpeedPower(type, player);
                    data.<EntityAttributeModifier>ifPresent
                        ("modifier", mod -> power.addModifier(AdditionalEntityAttributes.WATER_SPEED, mod));
                    data.<List<EntityAttributeModifier>>ifPresent("modifiers",
                        mods -> mods.forEach(mod -> power.addModifier(AdditionalEntityAttributes.WATER_SPEED, mod))
                    );
                    return power;
                })
            .allowCondition();
    }
}
