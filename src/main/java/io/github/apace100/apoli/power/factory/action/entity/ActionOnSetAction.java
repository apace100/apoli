package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.EntitySetPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
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

public class ActionOnSetAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
        PowerType<?> powerType = data.get("set");

        if (component == null || powerType == null || !(component.getPower(powerType) instanceof EntitySetPower entitySetPower)) {
            return;
        }

        Consumer<Pair<Entity, Entity>> biEntityAction = data.get("bientity_action");
        Predicate<Pair<Entity, Entity>> biEntityCondition = data.get("bientity_condition");

        List<UUID> uuids = new LinkedList<>(entitySetPower.getIterationSet());
        if (data.getBoolean("reverse")) {
            Collections.reverse(uuids);
        }

        int limit = data.get("limit");
        int processedUuids = 0;

        if (limit <= 0) {
            limit = Integer.MAX_VALUE;
        }

        for (UUID uuid : uuids) {

            Entity entityFromSet = entitySetPower.getEntity(uuid);
            Pair<Entity, Entity> entityPair = new Pair<>(entity, entityFromSet);

            if (biEntityCondition == null || biEntityCondition.test(entityPair)) {
                biEntityAction.accept(entityPair);
                processedUuids++;
            }

            if (processedUuids >= limit) {
                break;
            }

        }

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("action_on_set"),
            new SerializableData()
                .add("set", ApoliDataTypes.POWER_TYPE)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("limit", SerializableDataTypes.INT, 0)
                .add("reverse", SerializableDataTypes.BOOLEAN, false),
            ActionOnSetAction::action
        );
    }
}
