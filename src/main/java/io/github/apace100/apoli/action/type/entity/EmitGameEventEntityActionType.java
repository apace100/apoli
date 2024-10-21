package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.event.GameEvent;

public class EmitGameEventEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<EmitGameEventEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("event", SerializableDataTypes.GAME_EVENT_ENTRY),
        data -> new EmitGameEventEntityActionType(
            data.get("event")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("event", actionType.event)
    );

    private final RegistryEntry<GameEvent> event;

    public EmitGameEventEntityActionType(RegistryEntry<GameEvent> event) {
        this.event = event;
    }

    @Override
    protected void execute(Entity entity) {
        entity.emitGameEvent(event);
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.EMIT_GAME_EVENT;
    }

}
