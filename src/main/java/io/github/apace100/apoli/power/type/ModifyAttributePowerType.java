package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ModifyAttributePowerType extends ValueModifyingPowerType {

    public static final TypedDataObjectFactory<ModifyAttributePowerType> DATA_FACTORY = createConditionedModifyingDataFactory(
        new SerializableData()
            .add("attribute", SerializableDataTypes.ATTRIBUTE_ENTRY),
        (data, modifiers, condition) -> new ModifyAttributePowerType(
            data.get("attribute"),
            modifiers,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("attribute", powerType.attribute)
    );

    private final RegistryEntry<EntityAttribute> attribute;

    public ModifyAttributePowerType(RegistryEntry<EntityAttribute> attribute, List<Modifier> modifiers, Optional<EntityCondition> condition) {
        super(modifiers, condition);
        this.attribute = attribute;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_ATTRIBUTE;
    }

    public RegistryEntry<EntityAttribute> getAttribute() {
        return attribute;
    }

}
