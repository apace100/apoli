package io.github.apace100.apoli.power;

import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

import java.util.LinkedList;
import java.util.List;

public class AttributePower extends Power {

    private final List<AttributedEntityAttributeModifier> modifiers = new LinkedList<AttributedEntityAttributeModifier>();
    private final boolean updateHealth;

    public AttributePower(PowerType<?> type, LivingEntity entity, boolean updateHealth) {
        super(type, entity);
        this.updateHealth = updateHealth;
    }

    public AttributePower(PowerType<?> type, LivingEntity entity, boolean updateHealth, EntityAttribute attribute, EntityAttributeModifier modifier) {
        this(type, entity, updateHealth);
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
        float previousMaxHealth = entity.getMaxHealth();
        float previousHealthPercent = entity.getHealth() / previousMaxHealth;
        modifiers.forEach(mod -> {
            if(entity.getAttributes().hasAttribute(mod.getAttribute())) {
                entity.getAttributeInstance(mod.getAttribute()).addTemporaryModifier(mod.getModifier());
            }
        });
        float afterMaxHealth = entity.getMaxHealth();
        if(updateHealth && afterMaxHealth != previousMaxHealth) {
            entity.setHealth(afterMaxHealth * previousHealthPercent);
        }
    }

    @Override
    public void onRemoved() {
        float previousMaxHealth = entity.getMaxHealth();
        float previousHealthPercent = entity.getHealth() / previousMaxHealth;
        modifiers.forEach(mod -> {
            if (entity.getAttributes().hasAttribute(mod.getAttribute())) {
                entity.getAttributeInstance(mod.getAttribute()).removeModifier(mod.getModifier());
            }
        });
        float afterMaxHealth = entity.getMaxHealth();
        if(updateHealth && afterMaxHealth != previousMaxHealth) {
            entity.setHealth(afterMaxHealth * previousHealthPercent);
        }
    }
}
