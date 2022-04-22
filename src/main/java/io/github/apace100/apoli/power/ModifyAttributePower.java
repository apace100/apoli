package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;

import java.util.List;

public class ModifyAttributePower extends ValueModifyingPower {

    private final EntityAttribute attribute;

    public ModifyAttributePower(PowerType<?> type, LivingEntity entity, EntityAttribute attribute) {
        super(type, entity);
        this.attribute = attribute;
    }

    public EntityAttribute getAttribute() {
        return attribute;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_attribute"),
            new SerializableData()
                .add("attribute", SerializableDataTypes.ATTRIBUTE)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data ->
                (type, player) -> {
                    ModifyAttributePower power = new ModifyAttributePower(type, player, data.get("attribute"));
                    data.ifPresent("modifier", power::addModifier);
                    data.<List<Modifier>>ifPresent("modifiers",
                        mods -> mods.forEach(power::addModifier)
                    );
                    return power;
                })
            .allowCondition();
    }
}
