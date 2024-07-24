package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.OwnableAttributeContainer;
import io.github.apace100.apoli.access.OwnableAttributeInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AttributeContainer.class)
public abstract class AttributeContainerMixin implements OwnableAttributeContainer {

    @Shadow
    @Final
    private DefaultAttributeContainer fallback;

    @Unique
    @Nullable
    private Entity apoli$owner;

    @Override
    @Nullable
    public Entity apoli$getOwner() {
        return apoli$owner;
    }

    @Override
    public void apoli$setOwner(Entity owner) {
        this.apoli$owner = owner;
    }

    @Inject(method = "getCustomInstance", at = @At("RETURN"))
    private void apoli$setCustomAttributeInstanceOwner(RegistryEntry<EntityAttribute> attribute, CallbackInfoReturnable<EntityAttributeInstance> cir) {

        if (cir.getReturnValue() instanceof OwnableAttributeInstance ownableAttributeInstance) {
            ownableAttributeInstance.apoli$setOwner(this.apoli$getOwner());
        }

    }

    @Inject(method = "getValue", at = @At("RETURN"))
    private void apoli$setAttributeInstanceOwner(RegistryEntry<EntityAttribute> attribute, CallbackInfoReturnable<Double> cir, @Local EntityAttributeInstance attributeInstance) {

        if (attributeInstance instanceof OwnableAttributeInstance ownableAttributeInstance) {
            ownableAttributeInstance.apoli$setOwner(this.apoli$getOwner());
        }

        if (this.fallback instanceof OwnableAttributeContainer ownableAttributeContainer) {
            ownableAttributeContainer.apoli$setOwner(this.apoli$getOwner());
        }

    }

}
