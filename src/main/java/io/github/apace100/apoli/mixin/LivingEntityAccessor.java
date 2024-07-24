package io.github.apace100.apoli.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("SPRINTING_SPEED_BOOST")
    static EntityAttributeModifier apoli$getSprintingSpeedBoost() {
        throw new RuntimeException();
    }
}
