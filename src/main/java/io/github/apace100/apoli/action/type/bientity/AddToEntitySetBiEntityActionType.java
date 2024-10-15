package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.EntitySetPowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class AddToEntitySetBiEntityActionType extends BiEntityActionType {

    public static final DataObjectFactory<AddToEntitySetBiEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("set", ApoliDataTypes.POWER_REFERENCE)
            .add("time_limit", SerializableDataTypes.POSITIVE_INT.optional(), Optional.empty()),
        data -> new AddToEntitySetBiEntityActionType(
            data.get("set"),
            data.get("time_limit")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("set", actionType.set)
            .set("time_limit", actionType.timeLimit)
    );

    private final PowerReference set;
    private final Optional<Integer> timeLimit;

    public AddToEntitySetBiEntityActionType(PowerReference set, Optional<Integer> timeLimit) {
        this.set = set;
        this.timeLimit = timeLimit;
    }

    @Override
    public void execute(Entity actor, Entity target) {

        if (set.getType(actor) instanceof EntitySetPowerType entitySet && entitySet.add(target, timeLimit)) {
            PowerHolderComponent.syncPower(actor, set);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BiEntityActionTypes.ADD_TO_ENTITY_SET;
    }

}
