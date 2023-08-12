package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.EntityAttributeInstanceAccess;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyAttributePower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityAttributeInstance.class)
public abstract class EntityAttributeInstanceMixin implements EntityAttributeInstanceAccess {

    @Shadow
    @Final
    private EntityAttribute type;

    @Unique
    @Nullable
    Entity apoli$entity;

    @Override
    public void apoli$setEntity(Entity entity) {
        apoli$entity = entity;
    }

    @Override
    public @Nullable Entity apoli$getEntity() {
        return apoli$entity;
    }

    @ModifyReturnValue(method = "getValue", at = @At("RETURN"))
    private double apoli$modifyAttributeValue(double original) {
        return PowerHolderComponent.modify(apoli$entity, ModifyAttributePower.class, (float) original, p -> p.getAttribute() == type);
    }

}
