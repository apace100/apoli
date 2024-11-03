package io.github.apace100.apoli.power.type;

import de.dafuqs.additionalentityattributes.AdditionalEntityAttributes;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

@Deprecated
public class ModifySwimSpeedPowerType extends ConditionedAttributePowerType {

    public static final TypedDataObjectFactory<ModifySwimSpeedPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("modifier", SerializableDataTypes.ATTRIBUTE_MODIFIER, null)
            .addFunctionedDefault("modifiers", SerializableDataTypes.ATTRIBUTE_MODIFIER.list(1, Integer.MAX_VALUE), data -> MiscUtil.singletonListOrNull(data.get("modifier")))
            .validate(MiscUtil.validateAnyFieldsPresent("modifier", "modifiers")),
        (data, condition) -> new ModifySwimSpeedPowerType(
            data.get("modifiers"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("modifiers", powerType.attributeModifiers)
    );

    private final List<EntityAttributeModifier> attributeModifiers;

    public ModifySwimSpeedPowerType(List<EntityAttributeModifier> attributeModifiers, Optional<EntityCondition> condition) {
        super(attributeModifiers.stream().map(attributeModifier -> new AttributedEntityAttributeModifier(AdditionalEntityAttributes.WATER_SPEED, attributeModifier)).toList(), false, 10, condition);
        this.attributeModifiers = attributeModifiers;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_SWIM_SPEED;
    }

}
