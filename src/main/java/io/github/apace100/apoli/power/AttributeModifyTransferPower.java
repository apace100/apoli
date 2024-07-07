package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.ClassDataRegistry;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.mixin.EntityAttributeModifierAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.List;

public class AttributeModifyTransferPower extends Power {

    private final Class<?> modifyClass;
    private final EntityAttribute attribute;
    private final double valueMultiplier;

    public AttributeModifyTransferPower(PowerType<?> type, LivingEntity entity, Class<?> modifyClass, EntityAttribute attribute, double valueMultiplier) {
        super(type, entity);
        this.modifyClass = modifyClass;
        this.attribute = attribute;
        this.valueMultiplier = valueMultiplier;
    }

    public boolean doesApply(Class<?> cls) {
        return cls.equals(modifyClass);
    }

    public void addModifiers(List<Modifier> modifiers) {
        AttributeContainer attrContainer = entity.getAttributes();
        if(attrContainer.hasAttribute(attribute)) {
            EntityAttributeInstance attributeInstance = attrContainer.getCustomInstance(attribute);
            attributeInstance.getModifiers().forEach(mod -> {
                EntityAttributeModifier transferMod =
                    new EntityAttributeModifier(((EntityAttributeModifierAccessor) mod).getName(), mod.getValue() * valueMultiplier, mod.getOperation());
                modifiers.add(ModifierUtil.fromAttributeModifier(transferMod));
            });
        }
    }

    public void apply(List<EntityAttributeModifier> modifiers) {
        AttributeContainer attrContainer = entity.getAttributes();
        if(attrContainer.hasAttribute(attribute)) {
            EntityAttributeInstance attributeInstance = attrContainer.getCustomInstance(attribute);
            attributeInstance.getModifiers().forEach(mod -> {
                EntityAttributeModifier transferMod =
                    new EntityAttributeModifier(((EntityAttributeModifierAccessor) mod).getName(), mod.getValue() * valueMultiplier, mod.getOperation());
                modifiers.add(transferMod);
            });
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("attribute_modify_transfer"),
            new SerializableData()
                .add("class", ClassDataRegistry.get(Power.class).get().getDataType())
                .add("attribute", SerializableDataTypes.ATTRIBUTE)
                .add("multiplier", SerializableDataTypes.DOUBLE, 1.0),
            data ->
                (type, player) -> new AttributeModifyTransferPower(type, player,
                    (Class<?>)data.get("class"),
                    (EntityAttribute)data.get("attribute"),
                    data.getDouble("multiplier")))
            .allowCondition();
    }
}
