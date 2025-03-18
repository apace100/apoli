package io.github.apace100.apoli.mixin.internal;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Proxy to propagate
 *
 * @author Ampflower
 **/
@ApiStatus.Internal
@Mixin(DefaultAttributeRegistry.class)
public interface DefaultAttributeRegistryAccessor {

    @Accessor("DEFAULT_ATTRIBUTE_REGISTRY")
    static Map<EntityType<? extends LivingEntity>, DefaultAttributeContainer> apoli$getRegistry() {
        throw new AssertionError();
    }
}
