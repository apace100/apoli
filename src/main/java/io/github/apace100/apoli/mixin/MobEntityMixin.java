package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ModifiableMobWithGoals;
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
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(MobEntity.class)
@Implements(@Interface(iface = ModifiableMobWithGoals.class, prefix = "apoli$"))
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

    @Unique
    private final List<Pair<MobBehavior, Goal>> apoli$modifiedTargetSelectorGoals = new ArrayList<>();
    @Unique
    private final List<Pair<MobBehavior, Goal>> apoli$modifiedGoalSelectorGoals = new ArrayList<>();

    public List<Pair<MobBehavior, Goal>> apoli$getModifiedTargetSelectorGoals() {
        return apoli$modifiedTargetSelectorGoals;
    }

    public List<Pair<MobBehavior, Goal>> apoli$getModifiedGoalSelectorGoals() {
        return apoli$modifiedGoalSelectorGoals;
    }
}