package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tag.Tag;
import net.minecraft.world.event.GameEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class PreventGameEventPower extends Power {

    private final Tag<GameEvent> tag;
    private final List<GameEvent> list;
    private final Consumer<Entity> entityAction;

    public PreventGameEventPower(PowerType<?> type, LivingEntity entity, Tag<GameEvent> tag, List<GameEvent> list, Consumer<Entity> entityAction) {
        super(type, entity);
        this.tag = tag;
        this.list = list;
        this.entityAction = entityAction;
    }

    public void executeAction(Entity entity) {
        if(entityAction != null) {
            entityAction.accept(entity);
        }
    }

    public boolean doesPrevent(GameEvent event) {
        if(tag != null && tag.contains(event)) {
            return true;
        }
        if(list != null && list.contains(event)) {
            return true;
        }
        return false;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_game_event"),
            new SerializableData()
                .add("event", SerializableDataTypes.GAME_EVENT, null)
                .add("events", SerializableDataTypes.GAME_EVENTS, null)
                .add("tag", SerializableDataTypes.GAME_EVENT_TAG, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            data ->
                (type, player) -> {
                    List<GameEvent> eventList = data.isPresent("events") ? (List<GameEvent>)data.get("events") : null;
                    if(data.isPresent("event")) {
                        if(eventList == null) {
                            eventList = new LinkedList<>();
                        }
                        eventList.add((GameEvent)data.get("event"));
                    }
                    return new PreventGameEventPower(type, player,
                        (Tag<GameEvent>)data.get("tag"), eventList,
                        (ActionFactory<Entity>.Instance)data.get("entity_action"));
                })
            .allowCondition();
    }
}
