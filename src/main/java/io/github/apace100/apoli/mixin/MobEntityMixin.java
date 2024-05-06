package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnItemPickupPower;
import io.github.apace100.apoli.power.ModifyMobBehaviorPower;
import io.github.apace100.apoli.power.PreventItemPickupPower;
import io.github.apace100.apoli.power.factory.behavior.types.LookMobBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements Targeter {

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapWithCondition(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;loot(Lnet/minecraft/entity/ItemEntity;)V"))
    private boolean apoli$onItemPickup(MobEntity instance, ItemEntity itemEntity) {

        if (PreventItemPickupPower.doesPrevent(itemEntity, this)) {
            return false;
        }

        ActionOnItemPickupPower.executeActions(itemEntity, this);
        return true;

    }

    @ModifyVariable(method = "setTarget", at = @At("HEAD"), argsOnly = true)
    private LivingEntity modifyTarget(LivingEntity target) {
        if (getWorld().isClient() || target == null) {
            return target;
        }

        List<ModifyMobBehaviorPower> modifyMobBehaviorPowers = PowerHolderComponent.getPowers(this, ModifyMobBehaviorPower.class);

        boolean shouldMakePassive = modifyMobBehaviorPowers.stream().anyMatch(power -> power.getMobBehavior().isPassive(target));

        if (shouldMakePassive) {
            if (this instanceof Angerable) {
                ((Angerable)this).stopAnger();
            }
            return null;
        }

        return target;
    }

    @Inject(method = "tickNewAi", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/control/MoveControl;tick()V", shift = At.Shift.AFTER))
    private void apoli$tickBodyMovement(CallbackInfo ci) {
        PowerHolderComponent.getPowers(this, ModifyMobBehaviorPower.class).stream().filter(power -> power.getMobBehavior() instanceof LookMobBehavior look && look.shouldMoveBody()).sorted(Comparator.comparingInt(value -> value.getMobBehavior().getPriority())).forEach(power -> {
            ((LookMobBehavior)power.getMobBehavior()).tickBody();
        });
    }

}
