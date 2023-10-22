package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.PowerLinkedListenerData;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.GameEventTags;
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
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GameEventListenerPower extends CooldownPower implements Vibrations {

    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;
    private final Consumer<Pair<Entity, Entity>> biEntityAction;

    private final Predicate<CachedBlockPosition> blockCondition;
    private final Predicate<Pair<Entity, Entity>> biEntityCondition;

    private final List<GameEvent> acceptedGameEvents;
    private final TagKey<GameEvent> acceptedGameEventTag;

    private EntityGameEventHandler<VibrationListener> gameEventHandler;

    private final GameEventListener.TriggerOrder triggerOrder;
    private final ListenerData vibrationListenerData;
    private final Callback vibrationCallback;

    private final boolean listenToEntityEvents;
    private final boolean listenToBlockEvents;
    private final boolean showParticle;
    private final int range;

    public GameEventListenerPower(PowerType<?> powerType, LivingEntity livingEntity, Consumer<Triple<World, BlockPos, Direction>> blockAction, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<CachedBlockPosition> blockCondition, Predicate<Pair<Entity, Entity>> biEntityCondition, int cooldownDuration, HudRender hudRender, int range, GameEvent acceptedGameEvent, List<GameEvent> acceptedGameEvents, TagKey<GameEvent> acceptedGameEventTag, boolean showParticle, boolean listenToEntityEvents, boolean listenToBlockEvents, GameEventListener.TriggerOrder triggerOrder) {
        super(powerType, livingEntity, Math.max(cooldownDuration, 1), hudRender);

        this.blockAction = blockAction;
        this.biEntityAction = biEntityAction;
        this.blockCondition = blockCondition;
        this.biEntityCondition = biEntityCondition;
        this.range = Math.max(range, 1);

        this.acceptedGameEvents = new LinkedList<>();
        if (acceptedGameEvent != null) {
            this.acceptedGameEvents.add(acceptedGameEvent);
        }
        if (acceptedGameEvents != null) {
            this.acceptedGameEvents.addAll(acceptedGameEvents);
        }

        this.gameEventHandler = null;
        this.triggerOrder = triggerOrder;

        this.vibrationListenerData = new ListenerData();
        this.vibrationCallback = new Callback();

        this.acceptedGameEventTag = acceptedGameEventTag;
        this.showParticle = showParticle;
        this.listenToEntityEvents = listenToEntityEvents;
        this.listenToBlockEvents = listenToBlockEvents;
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
        return canListen() && super.canUse();
    }

    @Override
    public ListenerData getVibrationListenerData() {
        ((PowerLinkedListenerData) vibrationListenerData).apoli$setPower(this);
        return vibrationListenerData;
    }

    @Override
    public Callback getVibrationCallback() {
        return vibrationCallback;
    }

    public boolean canListen() {
        return gameEventHandler != null
            && gameEventHandler.getListener() != null;
    }

    public EntityGameEventHandler<VibrationListener> getGameEventHandler() {

        if (!canListen()) {
            gameEventHandler = new EntityGameEventHandler<>(new Listener());
        }

        return gameEventHandler;

    }

    public boolean shouldListenToEntityEvents(GameEvent.Emitter emitter) {
        return listenToEntityEvents && (biEntityCondition == null || biEntityCondition.test(new Pair<>(emitter.sourceEntity(), entity)));
    }

    public boolean shouldListenToBlockEvents(GameEvent.Emitter emitter, CachedBlockPosition cachedBlockPosition) {
        return listenToBlockEvents && (emitter.sourceEntity() == null && (blockCondition == null || blockCondition.test(cachedBlockPosition)));
    }

    public boolean shouldShowParticle() {
        return showParticle;
    }

    public class Listener extends VibrationListener {

        public Listener() {
            super(GameEventListenerPower.this);
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
        public boolean accepts(ServerWorld world, BlockPos pos, GameEvent event, GameEvent.Emitter emitter) {
            return entity.getWorld() == world
                && (shouldListenToBlockEvents(emitter, new CachedBlockPosition(world, pos, true))
                || shouldListenToEntityEvents(emitter));
        }

        @Override
        public void accept(ServerWorld world, BlockPos pos, GameEvent event, @Nullable Entity sourceEntity, @Nullable Entity projectileOwner, float distance) {

            use();
            if (blockAction != null) {
                blockAction.accept(Triple.of(world, pos, Direction.UP));
            }

            if (biEntityAction != null) {
                biEntityAction.accept(new Pair<>(sourceEntity, GameEventListenerPower.this.entity));
            }

        }

        @Override
        public boolean canAccept(GameEvent gameEvent, GameEvent.Emitter emitter) {
            return canUse()
                && Vibrations.Callback.super.canAccept(gameEvent, emitter)
                && (acceptedGameEvents.isEmpty() || acceptedGameEvents.contains(gameEvent));
        }

        @Override
        public TagKey<GameEvent> getTag() {
            return acceptedGameEventTag;
        }

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("game_event_listener"),
            new SerializableData()
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("cooldown", SerializableDataTypes.INT, 1)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                .add("range", SerializableDataTypes.INT, 16)
                .add("event", SerializableDataTypes.GAME_EVENT, null)
                .add("events", SerializableDataTypes.GAME_EVENTS, null)
                .add("tag", SerializableDataTypes.GAME_EVENT_TAG, GameEventTags.VIBRATIONS)
                .addFunctionedDefault("event_tag", SerializableDataTypes.GAME_EVENT_TAG, data -> data.get("tag"))
                .add("show_particle", SerializableDataTypes.BOOLEAN, true)
                .add("entity", SerializableDataTypes.BOOLEAN, true)
                .add("block", SerializableDataTypes.BOOLEAN, true)
                .add("trigger_order", SerializableDataType.enumValue(GameEventListener.TriggerOrder.class), GameEventListener.TriggerOrder.UNSPECIFIED),
            data -> (powerType, livingEntity) -> new GameEventListenerPower(
                powerType,
                livingEntity,
                data.get("block_action"),
                data.get("bientity_action"),
                data.get("block_condition"),
                data.get("bientity_condition"),
                data.get("cooldown"),
                data.get("hud_render"),
                data.get("range"),
                data.get("event"),
                data.get("events"),
                data.get("event_tag"),
                data.get("show_particle"),
                data.get("entity"),
                data.get("block"),
                data.get("trigger_order")
            )
        ).allowCondition();
    }

}
