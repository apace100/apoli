package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.List;

public class ModifyAttributePowerType extends ValueModifyingPowerType {

    private final RegistryEntry<EntityAttribute> attribute;

    public ModifyAttributePowerType(Power power, LivingEntity entity, RegistryEntry<EntityAttribute> attribute, Modifier modifier, List<Modifier> modifiers) {
        super(power, entity);

        this.attribute = attribute;
        if (modifier != null) {
            this.addModifier(modifier);
        }

        if (modifiers != null) {
            modifiers.forEach(this::addModifier);
        }

    }

    public RegistryEntry<EntityAttribute> getAttribute() {
        return attribute;
    }

    public static PowerTypeFactory<ModifyAttributePowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_attribute"),
            new SerializableData()
                .add("attribute", SerializableDataTypes.ATTRIBUTE_ENTRY)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data -> (power, entity) -> new ModifyAttributePowerType(power, entity,
                data.get("attribute"),
                data.get("modifier"),
                data.get("modifiers")
            )
        ).allowCondition();
    }

}
