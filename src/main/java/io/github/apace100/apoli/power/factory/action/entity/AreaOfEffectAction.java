package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class AreaOfEffectAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        Consumer<Pair<Entity, Entity>> biEntityAction = data.get("bientity_action");
        Predicate<Pair<Entity, Entity>> biEntityCondition = data.get("bientity_condition");
        Shape shape = data.get("shape");

        boolean includeActor = data.get("include_actor");
        double radius = data.get("radius");

        for (Entity target : Shape.getEntities(shape, entity.getWorld(), entity.getLerpedPos(1.0f), radius)) {

            if (target == entity && !includeActor) {
                continue;
            }

            Pair<Entity, Entity> actorAndTarget = new Pair<>(entity, target);
            if (biEntityCondition == null || biEntityCondition.test(actorAndTarget)) {
                biEntityAction.accept(actorAndTarget);
            }

        }

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("area_of_effect"),
            new SerializableData()
                .add("radius", SerializableDataTypes.DOUBLE, 16D)
                .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("include_target", SerializableDataTypes.BOOLEAN, false)
                .addFunctionedDefault("include_actor", SerializableDataTypes.BOOLEAN, data -> data.get("include_target")),
            AreaOfEffectAction::action
        );
    }

}

