package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PreventGameEventPowerType extends PowerType {

    public static final TypedDataObjectFactory<PreventGameEventPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("event", SerializableDataTypes.GAME_EVENT_ENTRY, null)
            .addFunctionedDefault("events", SerializableDataTypes.GAME_EVENT_ENTRIES, data -> MiscUtil.singletonListOrEmpty(data.get("event")))
            .add("event_tag", SerializableDataTypes.GAME_EVENT_TAG.optional(), Optional.empty()),
        (data, condition) -> new PreventGameEventPowerType(
            data.get("entity_action"),
            data.get("events"),
            data.get("event_tag"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
            .set("events", powerType.events)
            .set("event_tag", powerType.eventTag)
    );

    private final Optional<EntityAction> entityAction;

    private final List<RegistryEntry<GameEvent>> events;
    private final Optional<TagKey<GameEvent>> eventTag;

    public PreventGameEventPowerType(Optional<EntityAction> entityAction, List<RegistryEntry<GameEvent>> events, Optional<TagKey<GameEvent>> eventTag, Optional<EntityCondition> condition) {
        super(condition);
        this.entityAction = entityAction;
        this.events = events
            .stream()
            .distinct()
            .collect(Collectors.toCollection(ObjectArrayList::new));
        this.eventTag = eventTag;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_GAME_EVENT;
    }

    public void executeAction() {
        entityAction.ifPresent(action -> action.execute(getHolder()));
    }

    public boolean doesPrevent(RegistryEntry<GameEvent> event) {
        return eventTag.map(event::isIn).orElse(false)
            || (events.isEmpty() || events.contains(event));
    }

}
