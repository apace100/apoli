package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.event.GameEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class PreventGameEventPower extends Power {

    private final List<RegistryEntry<GameEvent>> events;
    private final TagKey<GameEvent> eventTag;

    private final Consumer<Entity> entityAction;

    public PreventGameEventPower(PowerType<?> type, LivingEntity entity, RegistryEntry<GameEvent> event, List<RegistryEntry<GameEvent>> events, TagKey<GameEvent> eventTag, Consumer<Entity> entityAction) {
        super(type, entity);

        this.events = new LinkedList<>();
        this.eventTag = eventTag;
        this.entityAction = entityAction;

        if (event != null) {
            this.events.add(event);
        }

        if (events != null) {
            this.events.addAll(events);
        }

    }

    public void executeAction() {
        if (entityAction != null) {
            entityAction.accept(entity);
        }
    }

    public boolean doesPrevent(RegistryEntry<GameEvent> event) {
        return (eventTag != null && event.isIn(eventTag))
            || (events != null && events.contains(event));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("prevent_game_event"),
            new SerializableData()
                .add("event", SerializableDataTypes.GAME_EVENT_ENTRY, null)
                .add("events", SerializableDataTypes.GAME_EVENT_ENTRIES, null)
                .add("tag", SerializableDataTypes.GAME_EVENT_TAG, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            data -> (type, entity) -> new PreventGameEventPower(
                type,
                entity,
                data.get("event"),
                data.get("events"),
                data.get("tag"),
                data.get("entity_action")
            )
        ).allowCondition();
    }
}
