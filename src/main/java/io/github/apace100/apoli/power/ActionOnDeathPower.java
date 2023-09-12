package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnDeathPower extends Power {

    private final Predicate<Pair<DamageSource, Float>> damageCondition;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;
    private final Consumer<Pair<Entity, Entity>> bientityAction;

    public ActionOnDeathPower(PowerType<?> type, LivingEntity entity, Predicate<Pair<DamageSource, Float>> damageCondition, Consumer<Pair<Entity, Entity>> bientityAction, Predicate<Pair<Entity, Entity>> bientityCondition) {
        super(type, entity);
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

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("action_on_death"),
            new SerializableData()
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            data ->
                (type, player) -> new ActionOnDeathPower(type, player, data.get("damage_condition"),
                        data.get("bientity_action"),
                        data.get("bientity_condition")))
            .allowCondition();
    }
}
