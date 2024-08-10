package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class RidingActionType {

    public static void action(Entity entity, Consumer<Entity> entityAction, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<Pair<Entity, Entity>> biEntityCondition, boolean recursive) {

        if (!entity.hasVehicle()) {
            return;
        }

        Entity vehicle = entity.getVehicle();
        if (recursive) {

            while (vehicle != null) {
                executeActions(entity, vehicle, entityAction, biEntityAction, biEntityCondition);
                vehicle = vehicle.getVehicle();
            }

        }

        else {
            executeActions(entity, vehicle, entityAction, biEntityAction, biEntityCondition);
        }

    }

    private static void executeActions(Entity actor, Entity target, Consumer<Entity> entityAction, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<Pair<Entity, Entity>> biEntityCondition) {

        Pair<Entity, Entity> actorAndTarget = new Pair<>(actor, target);

        if (biEntityCondition.test(actorAndTarget)) {
            entityAction.accept(target);
            biEntityAction.accept(actorAndTarget);
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("passenger_action"),
            new SerializableData()
                .add("action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("recursive", SerializableDataTypes.BOOLEAN, false),
            (data, entity) -> action(entity,
                data.getOrElse("action", e -> {}),
                data.getOrElse("bientity_action", actorAndTarget -> {}),
                data.getOrElse("bientity_condition", actorAndTarget -> true),
                data.get("recursive")
            )
        );
    }

}
