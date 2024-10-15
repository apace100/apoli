package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.EntitySetPowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;

public class RemoveFromEntitySetBiEntityActionType extends BiEntityActionType {

    public static final DataObjectFactory<RemoveFromEntitySetBiEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("set", ApoliDataTypes.POWER_REFERENCE),
        data -> new RemoveFromEntitySetBiEntityActionType(
            data.get("set")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("set", actionType.set)
    );

    private final PowerReference set;

    public RemoveFromEntitySetBiEntityActionType(PowerReference set) {
        this.set = set;
    }

    @Override
    public void execute(Entity actor, Entity target) {

        if (set.getType(actor) instanceof EntitySetPowerType entitySet && entitySet.remove(target)) {
            PowerHolderComponent.syncPower(actor, set);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BiEntityActionTypes.REMOVE_FROM_ENTITY_SET;
    }

}
