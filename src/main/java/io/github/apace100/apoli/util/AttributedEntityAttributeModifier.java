package io.github.apace100.apoli.util;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;

public record AttributedEntityAttributeModifier(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier) {

    @Deprecated
    public RegistryEntry<EntityAttribute> getAttribute() {
        return this.attribute();
    }

    @Deprecated
    public EntityAttributeModifier getModifier() {
        return this.modifier();
    }

}
