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

public class GrantPowerEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<GrantPowerEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("power", ApoliDataTypes.POWER_REFERENCE)
            .add("source", SerializableDataTypes.IDENTIFIER),
        data -> new GrantPowerEntityActionType(
            data.get("power"),
            data.get("source")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("power", actionType.power)
            .set("source", actionType.source)
    );

    private final PowerReference power;
    private final Identifier source;

    public GrantPowerEntityActionType(PowerReference power, Identifier source) {
        this.power = power;
        this.source = source;
    }

    @Override
    protected void execute(Entity entity) {
        PowerHolderComponent.grantPower(entity, power, source, true);
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.GRANT_POWER;
    }

}
