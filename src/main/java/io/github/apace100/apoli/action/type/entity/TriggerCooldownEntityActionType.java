package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.CooldownPowerType;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class TriggerCooldownEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<TriggerCooldownEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("power", ApoliDataTypes.POWER_REFERENCE),
        data -> new TriggerCooldownEntityActionType(
            data.get("power")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("power", actionType.power)
    );

    private final PowerReference power;

    public TriggerCooldownEntityActionType(PowerReference power) {
        this.power = power;
    }

    @Override
    protected void execute(Entity entity) {

        if (power.getType(entity) instanceof CooldownPowerType cooldownPowerType) {
            cooldownPowerType.use();
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.TRIGGER_COOLDOWN;
    }

}
