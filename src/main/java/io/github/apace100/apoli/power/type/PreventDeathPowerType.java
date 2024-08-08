package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
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

public class PreventDeathPowerType extends PowerType {

    private final Consumer<Entity> entityAction;
    private final Predicate<Pair<DamageSource, Float>> condition;

    public PreventDeathPowerType(Power power, LivingEntity entity, Consumer<Entity> entityAction, Predicate<Pair<DamageSource, Float>> condition) {
        super(power, entity);
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
        for (PreventDeathPowerType preventDeathPower : PowerHolderComponent.getPowerTypes(entity, PreventDeathPowerType.class)) {

            if (!preventDeathPower.doesApply(source, amount)) {
                continue;
            }

            preventDeathPower.executeAction();
            prevented = true;

        }

        return prevented;

    }

    public static PowerTypeFactory<PreventDeathPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("prevent_death"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null),
            data -> (power, entity) -> new PreventDeathPowerType(power, entity,
                data.get("entity_action"),
                data.get("damage_condition")
            )
        ).allowCondition();
    }

}
