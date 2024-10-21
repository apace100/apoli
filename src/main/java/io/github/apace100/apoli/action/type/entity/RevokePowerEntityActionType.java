package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class RevokePowerEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<RevokePowerEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("power", ApoliDataTypes.POWER_REFERENCE)
            .add("source", SerializableDataTypes.IDENTIFIER),
        data -> new RevokePowerEntityActionType(
            data.get("power"),
            data.get("source")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("power", actionType.power)
            .set("source", actionType.source)
    );

    private final PowerReference power;
    private final Identifier source;

    public RevokePowerEntityActionType(PowerReference power, Identifier source) {
        this.power = power;
        this.source = source;
    }

    @Override
    protected void execute(Entity entity) {
        PowerHolderComponent.revokePower(entity, power, source, true);
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.REVOKE_POWER;
    }

}
