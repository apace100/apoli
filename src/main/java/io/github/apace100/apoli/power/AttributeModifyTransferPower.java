package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.ClassDataRegistry;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.List;

public class AttributeModifyTransferPower extends Power {

    private final Class<?> modifyClass;
    private final RegistryEntry<EntityAttribute> attribute;

    private final double valueMultiplier;

    public AttributeModifyTransferPower(PowerType<?> type, LivingEntity entity, Class<?> modifyClass, RegistryEntry<EntityAttribute> attribute, double valueMultiplier) {
        super(type, entity);
        this.modifyClass = modifyClass;
        this.attribute = attribute;
        this.valueMultiplier = valueMultiplier;
    }

    public boolean doesApply(Class<?> cls) {
        return cls.equals(modifyClass);
    }

    public void addModifiers(List<Modifier> modifiers) {

        AttributeContainer attributeContainer = entity.getAttributes();
        if (!attributeContainer.hasAttribute(attribute)) {
            return;
        }

        EntityAttributeInstance attributeInstance = attributeContainer.getCustomInstance(attribute);
        if (attributeInstance != null) {
            attributeInstance.getModifiers()
                .stream()
                .map(mod -> new EntityAttributeModifier(mod.id(), mod.value() * valueMultiplier, mod.operation()))
                .map(ModifierUtil::fromAttributeModifier)
                .forEach(modifiers::add);
        }

    }

    @Deprecated(forRemoval = true)
    public void apply(List<EntityAttributeModifier> modifiers) {

        AttributeContainer attributeContainer = entity.getAttributes();
        if (!attributeContainer.hasAttribute(attribute)) {
            return;
        }

        EntityAttributeInstance attributeInstance = attributeContainer.getCustomInstance(attribute);
        if (attributeInstance != null) {
            attributeInstance.getModifiers()
                .stream()
                .map(mod -> new EntityAttributeModifier(mod.id(), mod.value() * valueMultiplier, mod.operation()))
                .forEach(modifiers::add);
        }

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("attribute_modify_transfer"),
            new SerializableData()
                .add("class", ClassDataRegistry.get(Power.class).orElseThrow().getDataType())
                .add("attribute", SerializableDataTypes.ATTRIBUTE_ENTRY)
                .add("multiplier", SerializableDataTypes.DOUBLE, 1.0),
            data -> (type, player) -> new AttributeModifyTransferPower(
                type,
                player,
                data.get("class"),
                data.get("attribute"),
                data.getDouble("multiplier")
            )
        ).allowCondition();
    }

}
