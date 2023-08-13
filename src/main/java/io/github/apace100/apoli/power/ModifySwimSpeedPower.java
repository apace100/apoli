package io.github.apace100.apoli.power;

import de.dafuqs.additionalentityattributes.AdditionalEntityAttributes;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;

import java.util.List;

public class ModifySwimSpeedPower extends ModifyAttributePower {

    public ModifySwimSpeedPower(PowerType<?> powerType, LivingEntity livingEntity, Modifier modifier, List<Modifier> modifiers) {
        super(powerType, livingEntity, AdditionalEntityAttributes.WATER_SPEED);
        if (modifier != null) {
            this.addModifier(modifier);
        }
        if (modifiers != null) {
            modifiers.forEach(this::addModifier);
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("modify_swim_speed"),
            new SerializableData()
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data -> (powerType, livingEntity) -> new ModifySwimSpeedPower(
                powerType,
                livingEntity,
                data.get("modifier"),
                data.get("modifier")
            )
        ).allowCondition();
    }

}
