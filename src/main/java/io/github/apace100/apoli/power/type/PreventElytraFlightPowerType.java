package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PreventElytraFlightPowerType extends PowerType {

    public static final TypedDataObjectFactory<PreventElytraFlightPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty()),
        (data, condition) -> new PreventElytraFlightPowerType(
            data.get("entity_action"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
    );

    private final Optional<EntityAction> entityAction;

    public PreventElytraFlightPowerType(Optional<EntityAction> entityAction, Optional<EntityCondition> condition) {
        super(condition);
        this.entityAction = entityAction;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_ELYTRA_FLIGHT;
    }

    public void executeAction() {
		entityAction.ifPresent(action -> action.execute(getHolder()));
    }

    //  FIXME: Fix the entity action not being executed when preventing elytra flight -eggohito
    public static boolean integrateAllowCallback(LivingEntity entity) {
        return !PowerHolderComponent.hasPowerType(entity, PreventElytraFlightPowerType.class);
    }

}
