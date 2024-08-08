package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.function.Consumer;

public class ActionOnLandPower extends Power {

    private final Consumer<Entity> entityAction;

    public ActionOnLandPower(PowerType type, LivingEntity entity, Consumer<Entity> entityAction) {
        super(type, entity);
        this.entityAction = entityAction;
    }

    public void executeAction() {
        entityAction.accept(entity);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("action_on_land"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            data ->
                (type, player) -> new ActionOnLandPower(type, player,
                    (ActionFactory<Entity>.Instance)data.get("entity_action")))
            .allowCondition();
    }
}
