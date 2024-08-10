package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnHitPowerType extends CooldownPowerType {

    private final Predicate<Pair<DamageSource, Float>> damageCondition;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;
    private final Consumer<Pair<Entity, Entity>> bientityAction;

    public ActionOnHitPowerType(Power power, LivingEntity entity, Consumer<Pair<Entity, Entity>> bientityAction, Predicate<Pair<Entity, Entity>> bientityCondition, Predicate<Pair<DamageSource, Float>> damageCondition, HudRender hudRender, int cooldownDuration) {
        super(power, entity, cooldownDuration, hudRender);
        this.damageCondition = damageCondition;
        this.bientityAction = bientityAction;
        this.bientityCondition = bientityCondition;
    }

    public boolean doesApply(Entity target, DamageSource source, float amount) {
        return this.canUse()
            && (bientityCondition == null || bientityCondition.test(new Pair<>(entity, target)))
            && (damageCondition == null || damageCondition.test(new Pair<>(source, amount)));
    }

    public void onHit(Entity target) {
        this.use();
        this.bientityAction.accept(new Pair<>(entity, target));
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("action_on_hit"),
            new SerializableData()
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                .add("cooldown", SerializableDataTypes.INT, 1),
            data -> (power, entity) -> new ActionOnHitPowerType(power, entity,
                data.get("bientity_action"),
                data.get("bientity_condition"),
                data.get("damage_condition"),
                data.get("hud_render"),
                data.get("cooldown")
            )
        ).allowCondition();
    }

}
