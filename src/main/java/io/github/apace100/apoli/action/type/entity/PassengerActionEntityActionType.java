package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class PassengerActionEntityActionType extends EntityActionType {

    public static final DataObjectFactory<PassengerActionEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("recursive", SerializableDataTypes.BOOLEAN, false),
        data -> new PassengerActionEntityActionType(
            data.get("action"),
            data.get("bientity_action"),
            data.get("bientity_condition"),
            data.get("recursive")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("action", actionType.entityAction)
            .set("bientity_action", actionType.biEntityAction)
            .set("bientity_condition", actionType.biEntityCondition)
            .set("recursive", actionType.recursive)
    );

    private final Optional<EntityAction> entityAction;
    private final Optional<BiEntityAction> biEntityAction;

    private final Optional<BiEntityCondition> biEntityCondition;
    private final boolean recursive;

    public PassengerActionEntityActionType(Optional<EntityAction> entityAction, Optional<BiEntityAction> biEntityAction, Optional<BiEntityCondition> biEntityCondition, boolean recursive) {
        this.entityAction = entityAction;
        this.biEntityAction = biEntityAction;
        this.biEntityCondition = biEntityCondition;
        this.recursive = recursive;
    }

    @Override
    protected void execute(Entity entity) {

        if (!entity.hasPassengers()) {
            return;
        }

        Iterable<Entity> passengers = recursive
            ? entity.getPassengersDeep()
            : entity.getPassengerList();

        for (Entity passenger : passengers) {

            BiEntityActionContext context = new BiEntityActionContext(passenger, entity);

            if (biEntityCondition.map(condition -> condition.test(context.conditionContext())).orElse(true)) {
                entityAction.ifPresent(action -> action.execute(entity));
                biEntityAction.ifPresent(action -> action.accept(context));
            }

        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.PASSENGER;
    }

}
