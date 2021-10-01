package io.github.apace100.apoli.power;

import io.github.apace100.apoli.util.HudRender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class TargetActionOnHitPower extends CooldownPower {

    private final Predicate<Pair<DamageSource, Float>> damageCondition;
    private final Predicate<Entity> targetCondition;
    private final Consumer<Entity> entityAction;

    public TargetActionOnHitPower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, Predicate<Pair<DamageSource, Float>> damageCondition, Consumer<Entity> entityAction, Predicate<Entity> targetCondition) {
        super(type, entity, cooldownDuration, hudRender);
        this.damageCondition = damageCondition;
        this.entityAction = entityAction;
        this.targetCondition = targetCondition;
    }

    public void onHit(LivingEntity target, DamageSource damageSource, float damageAmount) {
        if(targetCondition == null || targetCondition.test(target)) {
            if(damageCondition == null || damageCondition.test(new Pair<>(damageSource, damageAmount))) {
                if(canUse()) {
                    this.entityAction.accept(target);
                    use();
                }
            }
        }
    }
}
