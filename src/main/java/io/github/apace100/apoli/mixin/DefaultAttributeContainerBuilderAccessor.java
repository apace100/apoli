package io.github.apace100.apoli.mixin;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DefaultAttributeContainer.Builder.class)
public interface DefaultAttributeContainerBuilderAccessor {
    @Accessor
    Map<EntityAttribute, EntityAttributeInstance> getInstances();
}
