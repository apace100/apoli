package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.EntitySetPowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ActionOnEntitySetActionType {

    public static void action(Entity entity, PowerReference power, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<Pair<Entity, Entity>> biEntityCondition, boolean reverse, int limit) {

        if (!(power.getType(entity) instanceof EntitySetPowerType entitySet)) {
            return;
        }


        List<UUID> uuids = new LinkedList<>(entitySet.getIterationSet());
        if (reverse) {
            Collections.reverse(uuids);
        }

        int processedUuids = 0;
        if (limit <= 0) {
            limit = Integer.MAX_VALUE;
        }

        for (UUID uuid : uuids) {

            Entity entityFromSet = entitySet.getEntity(uuid);
            Pair<Entity, Entity> entityPair = new Pair<>(entity, entityFromSet);

            if (biEntityCondition.test(entityPair)) {
                biEntityAction.accept(entityPair);
                processedUuids++;
            }

            if (processedUuids >= limit) {
                break;
            }

        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("action_on_entity_set"),
            new SerializableData()
                .add("set", ApoliDataTypes.POWER_REFERENCE)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("reverse", SerializableDataTypes.BOOLEAN, false)
                .add("limit", SerializableDataTypes.INT, 0),
            (data, entity) -> action(entity,
                data.get("set"),
                data.get("bientity_action"),
                data.getOrElse("bientity_condition", actorAndTarget -> true),
                data.get("reverse"),
                data.get("limit")
            )
        );
    }

}
