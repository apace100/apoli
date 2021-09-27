package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyProjectileDamagePower extends ValueModifyingPower {

    private final Predicate<Pair<DamageSource, Float>> condition;
    private final Predicate<Entity> targetCondition;

    private Consumer<Entity> targetAction;
    private Consumer<Entity> selfAction;

    public ModifyProjectileDamagePower(PowerType<?> type, LivingEntity entity, Predicate<Pair<DamageSource, Float>> condition, Predicate<Entity> targetCondition) {
        super(type, entity);
        this.condition = condition;
        this.targetCondition = targetCondition;
    }

    public boolean doesApply(DamageSource source, float damageAmount, LivingEntity target) {
        return condition.test(new Pair<>(source, damageAmount)) && (target == null || targetCondition == null || targetCondition.test(target));
    }

    public void setTargetAction(Consumer<Entity> targetAction) {
        this.targetAction = targetAction;
    }

    public void setSelfAction(Consumer<Entity> selfAction) {
        this.selfAction = selfAction;
    }

    public void executeActions(Entity target) {
        if(selfAction != null) {
            selfAction.accept(entity);
        }
        if(targetAction != null) {
            targetAction.accept(target);
        }
    }
}
