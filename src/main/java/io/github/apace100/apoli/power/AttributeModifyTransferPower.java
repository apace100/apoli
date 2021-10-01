package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
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

    public void apply(List<EntityAttributeModifier> modifiers) {
        AttributeContainer attrContainer = entity.getAttributes();
        if(attrContainer.hasAttribute(attribute)) {
            EntityAttributeInstance attributeInstance = attrContainer.getCustomInstance(attribute);
            attributeInstance.getModifiers().forEach(mod -> {
                EntityAttributeModifier transferMod =
                    new EntityAttributeModifier(mod.getName(), mod.getValue() * valueMultiplier, mod.getOperation());
                Apoli.LOGGER.info("  Transferring attribute: " + mod.getName());
                modifiers.add(transferMod);
            });
        }
    }
}
