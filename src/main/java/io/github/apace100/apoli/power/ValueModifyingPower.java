package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

import java.util.LinkedList;
import java.util.List;

public class ValueModifyingPower extends Power {

    private final List<EntityAttributeModifier> modifiers = new LinkedList<>();

    public ValueModifyingPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public void addModifier(EntityAttributeModifier modifier) {
        this.modifiers.add(modifier);
    }

    public List<EntityAttributeModifier> getModifiers() {
        return modifiers;
    }
}
