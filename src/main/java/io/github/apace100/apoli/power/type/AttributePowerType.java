package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class AttributePowerType extends PowerType implements AttributeModifying {

    public static final TypedDataObjectFactory<AttributePowerType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("modifier", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIER, null)
            .addFunctionedDefault("modifiers", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIERS, data -> MiscUtil.singletonListOrNull(data.get("modifier")))
            .add("update_health", SerializableDataTypes.BOOLEAN, true)
            .validate(MiscUtil.validateAnyFieldsPresent("modifier", "modifiers")),
        data -> new AttributePowerType(
            data.get("modifiers"),
            data.get("update_health")
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("modifiers", powerType.attributedModifiers)
            .set("update_health", powerType.updateHealth)
    );

    private final List<AttributedEntityAttributeModifier> attributedModifiers;
    private final boolean updateHealth;

    public AttributePowerType(List<AttributedEntityAttributeModifier> attributedModifiers, boolean updateHealth, Optional<EntityCondition> condition) {
        super(condition);
        this.attributedModifiers = attributedModifiers;
        this.updateHealth = updateHealth;
    }

    public AttributePowerType(List<AttributedEntityAttributeModifier> attributedModifiers, boolean updateHealth) {
        this(attributedModifiers, updateHealth, Optional.empty());
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ATTRIBUTE;
    }

    //  Add/overwrite persistent modifiers when the entity is loaded in a world to ensure the entity
    //  keeps the modifiers in certain scenarios (e.g: respawning normally or when seeing the end credits for the first time)
    @Override
    public void onAdded() {
        addPersistentModifiers(getHolder());
    }

    @Override
    public void onLost() {
        removeModifiers(getHolder());
    }

    @Override
    public List<AttributedEntityAttributeModifier> attributedModifiers() {
        return attributedModifiers;
    }

    @Override
    public boolean shouldUpdateHealth() {
        return updateHealth;
    }

}
