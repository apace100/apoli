package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.function.Consumer;

public class ActionOnLandPowerType extends PowerType {

    private final Consumer<Entity> entityAction;

    public ActionOnLandPowerType(Power power, LivingEntity entity, Consumer<Entity> entityAction) {
        super(power, entity);
        this.entityAction = entityAction;
    }

    public void executeAction() {
        entityAction.accept(entity);
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("action_on_land"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            data -> (power, entity) -> new ActionOnLandPowerType(power, entity,
                data.get("entity_action")
            )
        ).allowCondition();
    }

}
