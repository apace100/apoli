package io.github.apace100.apoli.power;

import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

import java.util.LinkedList;
import java.util.List;

public class AttributePower extends Power {

    private final List<AttributedEntityAttributeModifier> modifiers = new LinkedList<AttributedEntityAttributeModifier>();

    public AttributePower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public AttributePower(PowerType<?> type, LivingEntity entity, EntityAttribute attribute, EntityAttributeModifier modifier) {
        this(type, entity);
        addModifier(attribute, modifier);
    }

    public AttributePower addModifier(EntityAttribute attribute, EntityAttributeModifier modifier) {
        AttributedEntityAttributeModifier mod = new AttributedEntityAttributeModifier(attribute, modifier);
        this.modifiers.add(mod);
        return this;
    }

    public AttributePower addModifier(AttributedEntityAttributeModifier modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    @Override
    public void onAdded() {
        modifiers.forEach(mod -> {
            if(entity.getAttributes().hasAttribute(mod.getAttribute())) {
                entity.getAttributeInstance(mod.getAttribute()).addTemporaryModifier(mod.getModifier());
            }
        });
    }

    @Override
    public void onRemoved() {
        modifiers.forEach(mod -> {
            if (entity.getAttributes().hasAttribute(mod.getAttribute())) {
                entity.getAttributeInstance(mod.getAttribute()).removeModifier(mod.getModifier());
            }
        });
    }
}
