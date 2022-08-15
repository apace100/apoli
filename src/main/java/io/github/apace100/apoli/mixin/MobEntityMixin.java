package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.MobEntityAccess;
import io.github.apace100.apoli.behavior.MobBehavior;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyMobBehaviorPower;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements MobEntityAccess {

    @Shadow public abstract void setTarget(@Nullable LivingEntity target);

    @Shadow @Final protected GoalSelector targetSelector;

    protected MobEntityMixin(EntityType<? extends LivingEntity> type, World level) {
        super(type, level);
    }

    @ModifyVariable(method = "setTarget", at = @At("HEAD"))
    private LivingEntity modifyTarget(LivingEntity target) {
        if (world.isClient() || target == null) {
            return target;
        }

        List<ModifyMobBehaviorPower> modifyMobBehaviorPowers = PowerHolderComponent.getPowers(target, ModifyMobBehaviorPower.class);
        boolean shouldMakePassive = modifyMobBehaviorPowers.stream().anyMatch(power -> power.doesApply(target, (MobEntity)(Object)this) && power.getMobBehavior().isPassive((MobEntity)(Object)this, target));

        if (shouldMakePassive) {
            if (this instanceof Angerable) {
                ((Angerable)this).stopAnger();
            }
            return null;
        }

        return target;
    }

    @Override
    protected void applyDamage(DamageSource source, float amount) {
        if (source.getAttacker() != null) {
            PowerHolderComponent.getPowers(source.getAttacker(), ModifyMobBehaviorPower.class).forEach(power -> {
                power.getMobBehavior().onMobDamage((MobEntity)(Object)this, source.getAttacker());
            });
        }
        super.applyDamage(source, amount);
    }

    private final List<Pair<MobBehavior, Goal>> modifiedTargetSelectorGoals = new ArrayList<>();
    private final List<Pair<MobBehavior, Goal>> modifiedGoalSelectorGoals = new ArrayList<>();

    @Override
    public List<Pair<MobBehavior, Goal>> getModifiedTargetSelectorGoals() {
        return modifiedTargetSelectorGoals;
    }

    @Override
    public List<Pair<MobBehavior, Goal>> getModifiedGoalSelectorGoals() {
        return modifiedGoalSelectorGoals;
    }
}