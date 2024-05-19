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

public class SelfActionOnHitPower extends CooldownPower {

    private final Predicate<Pair<DamageSource, Float>> damageCondition;
    private final Predicate<Entity> targetCondition;
    private final Consumer<Entity> entityAction;

    public SelfActionOnHitPower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, Predicate<Pair<DamageSource, Float>> damageCondition, Consumer<Entity> entityAction, Predicate<Entity> targetCondition) {
        super(type, entity, cooldownDuration, hudRender);
        this.damageCondition = damageCondition;
        this.entityAction = entityAction;
        this.targetCondition = targetCondition;
    }

    public boolean doesApply(Entity target, DamageSource source, float amount) {
        return this.canUse()
            && (targetCondition == null || targetCondition.test(target))
            && (damageCondition == null || damageCondition.test(new Pair<>(source, amount)));
    }

    public void onHit() {
        this.entityAction.accept(entity);
        this.use();
    }

    @Deprecated(forRemoval = true)
    public void onHit(Entity target, DamageSource damageSource, float damageAmount) {
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
        return new PowerFactory<>(Apoli.identifier("self_action_on_hit"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("cooldown", SerializableDataTypes.INT, 1)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                .add("target_condition", ApoliDataTypes.ENTITY_CONDITION, null),
            data -> (powerType, entity) -> new SelfActionOnHitPower(
                powerType,
                entity,
                data.get("cooldown"),
                data.get("hud_render"),
                data.get("damage_condition"),
                data.get("entity_action"),
                data.get("target_condition")
            )
        ).allowCondition();
    }

}
