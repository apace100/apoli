package io.github.apace100.apoli.power;

import io.github.apace100.apoli.util.HudRender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnHitPower extends CooldownPower {

    private final Predicate<Pair<DamageSource, Float>> damageCondition;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;
    private final Consumer<Pair<Entity, Entity>> bientityAction;

    public ActionOnHitPower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, Predicate<Pair<DamageSource, Float>> damageCondition, Consumer<Pair<Entity, Entity>> bientityAction, Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(type, entity, cooldownDuration, hudRender);
        this.damageCondition = damageCondition;
        this.bientityAction = bientityAction;
        this.bientityCondition = bientityCondition;
    }

    public void onHit(Entity target, DamageSource damageSource, float damageAmount) {
        if(canUse()) {
            if(bientityCondition == null || bientityCondition.test(new Pair<>(entity, target))) {
                if(damageCondition == null || damageCondition.test(new Pair<>(damageSource, damageAmount))) {
                    this.bientityAction.accept(new Pair<>(entity, target));
                    use();
                }
            }
        }
    }
}
