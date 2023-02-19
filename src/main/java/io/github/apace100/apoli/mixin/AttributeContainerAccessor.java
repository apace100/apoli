package io.github.apace100.apoli.mixin;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(AttributeContainer.class)
public interface AttributeContainerAccessor {
    @Accessor
    DefaultAttributeContainer getFallback();

    @Accessor @Mutable
    void setFallback(DefaultAttributeContainer value);

    @Accessor
    Set<EntityAttributeInstance> getTracked();
}
