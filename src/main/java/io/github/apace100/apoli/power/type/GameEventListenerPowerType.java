package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.EntityPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.Vibrations;
import net.minecraft.world.event.listener.EntityGameEventHandler;
import net.minecraft.world.event.listener.GameEventListener;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GameEventListenerPowerType extends CooldownPowerType implements Vibrations {

    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;
    private final Consumer<Pair<Entity, Entity>> biEntityAction;

    private final Predicate<CachedBlockPosition> blockCondition;
    private final Predicate<Pair<Entity, Entity>> biEntityCondition;

    private final List<RegistryEntry<GameEvent>> acceptedGameEvents;
    private final Optional<TagKey<GameEvent>> acceptedGameEventTag;

    private final GameEventListener.TriggerOrder triggerOrder;
    private final ListenerData vibrationListenerData;
    private final Callback vibrationCallback;

    private final boolean showParticle;
    private final int range;

    private EntityGameEventHandler<VibrationListener> gameEventHandler;

    public GameEventListenerPowerType(Power power, LivingEntity entity, Consumer<Pair<Entity, Entity>> biEntityAction, Consumer<Triple<World, BlockPos, Direction>> blockAction, Predicate<Pair<Entity, Entity>> biEntityCondition, Predicate<CachedBlockPosition> blockCondition, int cooldownDuration, HudRender hudRender, int range, Optional<RegistryEntry<GameEvent>> acceptedGameEvent, Optional<List<RegistryEntry<GameEvent>>> acceptedGameEvents, Optional<TagKey<GameEvent>> acceptedGameEventTag, boolean showParticle, GameEventListener.TriggerOrder triggerOrder) {
        super(power, entity, cooldownDuration, hudRender);

        this.blockAction = blockAction;
        this.biEntityAction = biEntityAction;
        this.blockCondition = blockCondition;
        this.biEntityCondition = biEntityCondition;
        this.range = range;

        this.acceptedGameEvents = new LinkedList<>();
        acceptedGameEvent.ifPresent(this.acceptedGameEvents::add);
        acceptedGameEvents.ifPresent(this.acceptedGameEvents::addAll);

        this.gameEventHandler = null;
        this.triggerOrder = triggerOrder;

        this.vibrationCallback = new Callback();
        this.vibrationListenerData = new ListenerData();
        this.vibrationListenerData.setSpawnParticle(false);

        this.acceptedGameEventTag = acceptedGameEventTag;
        this.showParticle = showParticle;
        this.setTicking();

    }

    @Override
    public void onAdded() {
        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            getGameEventHandler().onEntitySetPos(serverWorld);
        }
    }

    @Override
    public void onRemoved() {
        if (entity.getWorld() instanceof ServerWorld serverWorld) {
            getGameEventHandler().onEntityRemoval(serverWorld);
        }
    }

    @Override
    public void tick() {
        if (canUse()) {
            Ticker.tick(entity.getWorld(), getVibrationListenerData(), getVibrationCallback());
        }
    }

    @Override
    public boolean canUse() {
        return gameEventHandler != null && super.canUse();
    }

    @Override
    public ListenerData getVibrationListenerData() {
        return vibrationListenerData;
    }

    @Override
    public Callback getVibrationCallback() {
        return vibrationCallback;
    }

    public EntityGameEventHandler<VibrationListener> getGameEventHandler() {

        if (gameEventHandler == null) {
            gameEventHandler = new EntityGameEventHandler<>(new Listener());
        }

        return gameEventHandler;

    }

    public boolean shouldShowParticle() {
        return showParticle;
    }

    public class Listener extends VibrationListener {

        public Listener() {
            super(GameEventListenerPowerType.this);
        }

        @Override
        public TriggerOrder getTriggerOrder() {
            return triggerOrder;
        }

    }

    public class Callback implements Vibrations.Callback {

        @Override
        public int getRange() {
            return range;
        }

        @Override
        public PositionSource getPositionSource() {
            return new EntityPositionSource(entity, entity.getEyeHeight(entity.getPose()));
        }

        @Override
        public boolean accepts(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, GameEvent.Emitter emitter) {
            return blockCondition.test(new CachedBlockPosition(world, pos, true))
                && biEntityCondition.test(new Pair<>(emitter.sourceEntity(), entity));
        }

        @Override
        public void accept(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, @Nullable Entity sourceEntity, @Nullable Entity entity, float distance) {

            GameEventListenerPowerType.this.use();

            blockAction.accept(Triple.of(world, pos, Direction.UP));
            biEntityAction.accept(new Pair<>(sourceEntity, GameEventListenerPowerType.this.entity));

        }

        @Override
        public TagKey<GameEvent> getTag() {
            return acceptedGameEventTag.orElse(Vibrations.Callback.super.getTag());
        }

        public boolean shouldAccept(RegistryEntry<GameEvent> gameEvent) {
            return GameEventListenerPowerType.this.canUse()
                && this.isAccepted(gameEvent);
        }

        public boolean isAccepted(RegistryEntry<GameEvent> gameEvent) {
            return acceptedGameEventTag.map(gameEvent::isIn).orElse(true)
                && (acceptedGameEvents.isEmpty() || acceptedGameEvents.contains(gameEvent));
        }

    }

    public class ListenerData extends Vibrations.ListenerData {

        public boolean shouldShowParticle() {
            return GameEventListenerPowerType.this.shouldShowParticle();
        }

    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("game_event_listener"),
            new SerializableData()
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("cooldown", SerializableDataTypes.NON_NEGATIVE_INT, 1)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                .add("range", SerializableDataTypes.POSITIVE_INT, 16)
                .add("event", SerializableDataTypes.GAME_EVENT_ENTRY.optional(), Optional.empty())
                .add("events", SerializableDataTypes.GAME_EVENT_ENTRIES.optional(), Optional.empty())
                .add("event_tag", SerializableDataTypes.GAME_EVENT_TAG.optional(), Optional.empty())
                .add("show_particle", SerializableDataTypes.BOOLEAN, true)
                .add("trigger_order", SerializableDataType.enumValue(GameEventListener.TriggerOrder.class), GameEventListener.TriggerOrder.UNSPECIFIED),
            data -> (power, entity) -> new GameEventListenerPowerType(power, entity,
                data.getOrElse("bientity_action", actorAndTarget -> {}),
                data.getOrElse("block_action", block -> {}),
                data.getOrElse("bientity_condition", actorAndTarget -> true),
                data.getOrElse("block_condition", block -> true),
                data.get("cooldown"),
                data.get("hud_render"),
                data.get("range"),
                data.get("event"),
                data.get("events"),
                data.get("event_tag"),
                data.get("show_particle"),
                data.get("trigger_order")
            )
        ).allowCondition();
    }

}
