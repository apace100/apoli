package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.registry.ApoliClassData;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class AttributeModifyTransferPowerType extends PowerType {

    public static final Identifier ID = Apoli.identifier("attribute_modify_transfer");

    public static final TypedDataObjectFactory<AttributeModifyTransferPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("class", ApoliClassData.POWER_TYPE.getDataType())
            .add("attribute", SerializableDataTypes.ATTRIBUTE_ENTRY)
            .add("multiplier", SerializableDataTypes.DOUBLE, 1.0D),
        (data, condition) -> new AttributeModifyTransferPowerType(
            data.get("class"),
            data.get("attribute"),
            data.get("multiplier"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("class", powerType.modifyClass)
            .set("attribute", powerType.attribute)
            .set("multiplier", powerType.valueMultiplier)
    );

    private final Class<?> modifyClass;
    private final RegistryEntry<EntityAttribute> attribute;

    private final double valueMultiplier;

    public AttributeModifyTransferPowerType(Class<?> modifyClass, RegistryEntry<EntityAttribute> attribute, double valueMultiplier, Optional<EntityCondition> condition) {
        super(condition);
        this.modifyClass = modifyClass;
        this.attribute = attribute;
        this.valueMultiplier = valueMultiplier;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ATTRIBUTE_MODIFY_TRANSFER;
    }

    public boolean doesApply(Class<?> cls) {
        return cls.equals(modifyClass);
    }

    public static void registerCollectModifiersCallback(Entity entity, Class<? extends ValueModifyingPowerType> powerClass, double baseValue, List<Modifier> modifiers) {

        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        for (var transferPowerType : PowerHolderComponent.getPowerTypes(livingEntity, AttributeModifyTransferPowerType.class)) {

            AttributeContainer attributeContainer = livingEntity.getAttributes();
            EntityAttributeInstance attributeInstance = attributeContainer.getCustomInstance(transferPowerType.attribute);

            if (attributeInstance == null || !transferPowerType.doesApply(powerClass)) {
                continue;
            }

            for (var attributeModifier : attributeInstance.getModifiers()) {
                var modifiedAttributeModifier = new EntityAttributeModifier(attributeModifier.id(), attributeModifier.value() * transferPowerType.valueMultiplier, attributeModifier.operation());
                modifiers.add(ModifierUtil.fromAttributeModifier(modifiedAttributeModifier));
            }

        }

    }

}
