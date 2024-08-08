package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnDeathPowerType extends PowerType {

    private final Predicate<Pair<DamageSource, Float>> damageCondition;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;
    private final Consumer<Pair<Entity, Entity>> bientityAction;

    public ActionOnDeathPowerType(Power power, LivingEntity entity, Consumer<Pair<Entity, Entity>> bientityAction, Predicate<Pair<Entity, Entity>> bientityCondition, Predicate<Pair<DamageSource, Float>> damageCondition) {
        super(power, entity);
        this.damageCondition = damageCondition;
        this.bientityAction = bientityAction;
        this.bientityCondition = bientityCondition;
    }

    public boolean doesApply(Entity actor, DamageSource damageSource, float damageAmount) {
        return (bientityCondition == null || bientityCondition.test(new Pair<>(actor, entity)))
            && (damageCondition == null || damageCondition.test(new Pair<>(damageSource, damageAmount)));
    }

    public void onDeath(Entity actor) {
        bientityAction.accept(new Pair<>(actor, entity));
    }

    public static PowerTypeFactory<ActionOnDeathPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("action_on_death"),
            new SerializableData()
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null),
            data -> (power, entity) -> new ActionOnDeathPowerType(power, entity,
                data.get("bientity_action"),
                data.get("bientity_condition"),
                data.get("damage_condition")
            )
        ).allowCondition();
    }
}
