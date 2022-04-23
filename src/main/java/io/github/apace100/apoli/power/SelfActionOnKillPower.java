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

public class SelfActionOnKillPower extends CooldownPower {

    private final Predicate<Pair<DamageSource, Float>> damageCondition;
    private final Predicate<Entity> targetCondition;
    private final Consumer<Entity> entityAction;

    public SelfActionOnKillPower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, Predicate<Pair<DamageSource, Float>> damageCondition, Consumer<Entity> entityAction, Predicate<Entity> targetCondition) {
        super(type, entity, cooldownDuration, hudRender);
        this.damageCondition = damageCondition;
        this.entityAction = entityAction;
        this.targetCondition = targetCondition;
    }

    public void onKill(Entity target, DamageSource damageSource, float damageAmount) {
        if(targetCondition == null || targetCondition.test(target)) {
            if(damageCondition == null || damageCondition.test(new Pair<>(damageSource, damageAmount))) {
                if(canUse()) {
                    this.entityAction.accept(this.entity);
                    use();
                }
            }
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("self_action_on_kill"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("cooldown", SerializableDataTypes.INT, 1)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                .add("target_condition", ApoliDataTypes.ENTITY_CONDITION, null),
            data ->
                (type, player) -> new SelfActionOnKillPower(type, player, data.getInt("cooldown"),
                    (HudRender)data.get("hud_render"), (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition"),
                    (ActionFactory<Entity>.Instance)data.get("entity_action"),
                    (ConditionFactory<Entity>.Instance)data.get("target_condition")))
            .allowCondition();
    }
}
