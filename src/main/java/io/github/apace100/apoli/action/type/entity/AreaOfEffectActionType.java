package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class AreaOfEffectActionType {

    public static void action(Entity entity, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<Pair<Entity, Entity>> biEntityCondition, Shape shape, double radius, boolean includeActor) {

        for (Entity target : Shape.getEntities(shape, entity.getWorld(), entity.getLerpedPos(1.0f), radius)) {

            if (entity.equals(target) && !includeActor) {
                continue;
            }

            Pair<Entity, Entity> actorAndTarget = new Pair<>(entity, target);

            if (biEntityCondition.test(actorAndTarget)) {
                biEntityAction.accept(actorAndTarget);
            }

        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("area_of_effect"),
            new SerializableData()
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
                .add("radius", SerializableDataTypes.DOUBLE, 16D)
                .add("include_actor", SerializableDataTypes.BOOLEAN, false),
            (data, entity) -> action(entity,
                data.getOrElse("bientity_action", actorAndTarget -> {}),
                data.getOrElse("bientity_condition", actorAndTarget -> true),
                data.get("shape"),
                data.get("radius"),
                data.get("include_actor")
            )
        );
    }

}

