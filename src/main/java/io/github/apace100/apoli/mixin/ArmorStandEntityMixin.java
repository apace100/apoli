package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin extends LivingEntity {

    private ArmorStandEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "damage", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSource;isSourceCreativePlayer()Z")))
    private void apoli$invokeHitActions(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        if (!cir.getReturnValue()) {
            return;
        }

        Entity attacker = source.getAttacker();

        PowerHolderComponent.withPowerTypes(this, ActionWhenHitPowerType.class, p -> p.doesApply(attacker, source, amount), p -> p.whenHit(attacker));
        PowerHolderComponent.withPowerTypes(attacker, ActionOnHitPowerType.class, p -> p.doesApply(this, source, amount), p -> p.onHit(this));

        PowerHolderComponent.withPowerTypes(this, SelfActionWhenHitPowerType.class, p -> p.doesApply(source, amount), SelfActionWhenHitPowerType::whenHit);
        PowerHolderComponent.withPowerTypes(this, AttackerActionWhenHitPowerType.class, p -> p.doesApply(source, amount), p -> p.whenHit(attacker));

        PowerHolderComponent.withPowerTypes(attacker, SelfActionOnHitPowerType.class, p -> p.doesApply(this, source, amount), SelfActionOnHitPowerType::onHit);
        PowerHolderComponent.withPowerTypes(attacker, TargetActionOnHitPowerType.class, p -> p.doesApply(this, source, amount), p -> p.onHit(this));

    }

}
