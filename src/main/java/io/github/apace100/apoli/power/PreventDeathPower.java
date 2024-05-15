package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PreventDeathPower extends Power {

    private final Consumer<Entity> entityAction;
    private final Predicate<Pair<DamageSource, Float>> condition;

    public PreventDeathPower(PowerType<?> type, LivingEntity entity, Consumer<Entity> entityAction, Predicate<Pair<DamageSource, Float>> condition) {
        super(type, entity);
        this.entityAction = entityAction;
        this.condition = condition;
    }

    public boolean doesApply(DamageSource source, float amount) {
        return condition == null || condition.test(new Pair<>(source, amount));
    }

    public void executeAction() {
        if(entityAction != null) {
            entityAction.accept(entity);
        }
    }

    public static boolean doesPrevent(Entity entity, DamageSource source, float amount) {

        boolean prevented = false;
        for (PreventDeathPower preventDeathPower : PowerHolderComponent.getPowers(entity, PreventDeathPower.class)) {

            if (!preventDeathPower.doesApply(source, amount)) {
                continue;
            }

            preventDeathPower.executeAction();
            prevented = true;

        }

        return prevented;

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_death"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null),
            data ->
                (type, player) -> new PreventDeathPower(type, player,
                    (ActionFactory<Entity>.Instance)data.get("entity_action"),
                    (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition")))
            .allowCondition();
    }
}
