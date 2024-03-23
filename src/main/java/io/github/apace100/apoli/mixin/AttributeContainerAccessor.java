package io.github.apace100.apoli.mixin;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AttributeContainer.class)
public interface AttributeContainerAccessor {
    @Accessor
    Map<EntityAttribute, EntityAttributeInstance> getCustom();
}
