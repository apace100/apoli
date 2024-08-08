package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.function.Consumer;

public class PreventElytraFlightPowerType extends PowerType {

    private final Consumer<Entity> entityAction;

    public PreventElytraFlightPowerType(Power power, LivingEntity entity, Consumer<Entity> entityAction) {
        super(power, entity);
        this.entityAction = entityAction;
    }

    public void executeAction(Entity entity) {

        if (entityAction != null) {
            entityAction.accept(entity);
        }

    }

    public static PowerTypeFactory<?> getFactory() {

        //  FIXME: Fix the entity action not being executed when preventing elytra flight -eggohito
        EntityElytraEvents.ALLOW.register(entity -> !PowerHolderComponent.hasPowerType(entity, PreventElytraFlightPowerType.class));

        return new PowerTypeFactory<>(
            Apoli.identifier("prevent_elytra_flight"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            data -> (power, entity) -> new PreventElytraFlightPowerType(power, entity,
                data.get("entity_action")
            )
        ).allowCondition();

    }

}
