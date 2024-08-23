package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.event.GameEvent;

public class EmitGameEventActionType {

    public static void action(Entity entity, RegistryEntry<GameEvent> gameEvent) {
        entity.emitGameEvent(gameEvent);
    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("emit_game_event"),
            new SerializableData()
                .add("event", SerializableDataTypes.GAME_EVENT_ENTRY),
            (data, entity) -> action(entity,
                data.get("event")
            )
        );
    }

}
