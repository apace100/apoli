package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ModifiableMobWithGoals;
import io.github.apace100.apoli.behavior.types.HostileMobBehavior;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyMobBehaviorPower;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {

    @Shadow public abstract void setTarget(@Nullable LivingEntity target);

    protected MobEntityMixin(EntityType<? extends LivingEntity> type, World level) {
        super(type, level);
    }

    @ModifyVariable(method = "setTarget", at = @At("HEAD"))
    private LivingEntity modifyTarget(LivingEntity target) {
        if (world.isClient() || target == null) {
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