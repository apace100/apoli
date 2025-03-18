package io.github.apace100.apoli.mixin.internal;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * @author Ampflower
 **/
@ApiStatus.Internal
@Mixin(DefaultAttributeContainer.class)
public interface DefaultAttributeContainerAccessor {
    @Accessor
    Map<?, EntityAttributeInstance> getInstances();
}
