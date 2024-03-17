package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnItemPickupPower;
import io.github.apace100.apoli.power.ModifyMobBehaviorPower;
import io.github.apace100.apoli.power.PreventItemPickupPower;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements Targeter {

    @Shadow @Nullable private LivingEntity target;

    @Shadow @Final protected GoalSelector targetSelector;

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

}
