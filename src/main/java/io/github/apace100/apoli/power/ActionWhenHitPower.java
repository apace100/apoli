package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionWhenHitPower extends CooldownPower {

    private final Predicate<Pair<DamageSource, Float>> damageCondition;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;
    private final Consumer<Pair<Entity, Entity>> bientityAction;

    public ActionWhenHitPower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, Predicate<Pair<DamageSource, Float>> damageCondition, Consumer<Pair<Entity, Entity>> bientityAction, Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(type, entity, cooldownDuration, hudRender);
        this.damageCondition = damageCondition;
        this.bientityAction = bientityAction;
        this.bientityCondition = bientityCondition;
    }

    public void whenHit(Entity attacker, DamageSource damageSource, float damageAmount) {
        if(canUse()) {
            if(bientityCondition == null || bientityCondition.test(new Pair<>(attacker, entity))) {
                if(damageCondition == null || damageCondition.test(new Pair<>(damageSource, damageAmount))) {
                    this.bientityAction.accept(new Pair<>(attacker, entity));
                    use();
                }
            }
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("action_when_hit"),
            new SerializableData()
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("cooldown", SerializableDataTypes.INT, 1)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            data ->
                (type, player) -> new ActionWhenHitPower(type, player, data.getInt("cooldown"),
                    (HudRender)data.get("hud_render"), (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition"),
                    (ActionFactory<Pair<Entity, Entity>>.Instance)data.get("bientity_action"),
                    (ConditionFactory<Pair<Entity, Entity>>.Instance)data.get("bientity_condition")))
            .allowCondition();
    }
}
