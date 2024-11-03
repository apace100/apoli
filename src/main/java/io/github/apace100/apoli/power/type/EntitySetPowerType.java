package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class EntitySetPowerType extends PowerType {

    public static final TypedDataObjectFactory<EntitySetPowerType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("action_on_add", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("action_on_remove", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("tick_rate", SerializableDataTypes.POSITIVE_INT, 1),
        data -> new EntitySetPowerType(
            data.get("action_on_add"),
            data.get("action_on_remove"),
            data.get("tick_rate")
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("action_on_add", powerType.actionOnAdd)
            .set("action_on_remove", powerType.actionOnRemove)
            .set("tick_rate", powerType.tickRate)
    );

    private final Optional<BiEntityAction> actionOnAdd;
    private final Optional<BiEntityAction> actionOnRemove;

    private final int tickRate;

    private final Set<UUID> entityUuids = new HashSet<>();
    private final Map<UUID, Entity> entities = new HashMap<>();

    private final Set<UUID> tempUuids = new HashSet<>();
    private final Map<UUID, Long> tempEntities = new ConcurrentHashMap<>();

    private Integer startTicks = null;

    private boolean wasActive = false;
    private boolean removedTemps = false;

    public EntitySetPowerType(Optional<BiEntityAction> actionOnAdd, Optional<BiEntityAction> actionOnRemove, int tickRate) {
        this.actionOnAdd = actionOnAdd;
        this.actionOnRemove = actionOnRemove;
        this.tickRate = tickRate;
        this.setTicking(true);
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ENTITY_SET;
    }

    @Override
    public void onAdded() {
        this.removedTemps = entityUuids.removeIf(tempUuids::contains);
        this.tempUuids.clear();
    }

    @Override
    public void serverTick() {

        LivingEntity holder = getHolder();
        if (removedTemps) {

            this.removedTemps = false;
            PowerHolderComponent.syncPower(holder, this.getPower());

            return;

        }

        if (!tempEntities.isEmpty() && this.isActive()) {

            if (startTicks == null) {
                this.startTicks = holder.age % tickRate;
                return;
            }

            if (holder.age % tickRate == startTicks) {
                this.tickTempEntities();
            }

            this.wasActive = true;

        }

        else if (wasActive) {
            this.startTicks = null;
            this.wasActive = false;
        }

    }

    protected void tickTempEntities() {

        Iterator<Map.Entry<UUID, Long>> entryIterator = tempEntities.entrySet().iterator();
        LivingEntity holder = getHolder();

        long time = holder.getWorld().getTime();

        while (entryIterator.hasNext()) {

            Map.Entry<UUID, Long> entry = entryIterator.next();
            if (time < entry.getValue()) {
                continue;
            }

            UUID uuid = entry.getKey();
            Entity tempEntity = this.getEntity(uuid);

            entryIterator.remove();
            if (entityUuids.remove(uuid) | entities.remove(uuid) != null | tempUuids.remove(uuid)) {
                actionOnRemove.ifPresent(action -> action.execute(holder, tempEntity));
                this.removedTemps = true;
            }

        }

    }

    public boolean validateEntities() {

        MinecraftServer server = getHolder().getServer();
        if (server == null) {
            return false;
        }

        Iterator<UUID> uuidIterator = entityUuids.iterator();
        boolean valid = true;

        while (uuidIterator.hasNext()) {

            UUID uuid = uuidIterator.next();
            if (MiscUtil.getEntityByUuid(uuid, server) != null) {
                continue;
            }

            uuidIterator.remove();
            entities.remove(uuid);
            tempUuids.remove(uuid);
            tempEntities.remove(uuid);

            valid = false;

        }

        return valid;

    }

    public boolean add(Entity entity) {
        return add(entity, Optional.empty());
    }

    public boolean add(Entity entity, Optional<Integer> time) {

        if (entity == null || entity.isRemoved() || entity.getWorld().isClient) {
            return false;
        }

        UUID uuid = entity.getUuid();
        boolean addedToSet = false;

        if (time.isPresent()) {
            addedToSet |= tempUuids.add(uuid);
            tempEntities.compute(uuid, (prevUuid, prevTime) -> entity.getWorld().getTime() + time.get());
        }

        if (!entityUuids.contains(uuid)) {

            addedToSet |= entityUuids.add(uuid);
            entities.put(uuid, entity);

            actionOnAdd.ifPresent(action -> action.execute(getHolder(), entity));

        }

        return addedToSet;

    }

    public boolean remove(@Nullable Entity entity) {
        return this.remove(entity, true);
    }

    public boolean remove(@Nullable Entity entity, boolean executeRemoveAction) {

        if (entity == null || entity.getWorld().isClient) {
            return false;
        }

        UUID uuid = entity.getUuid();
        boolean result = entityUuids.remove(uuid)
            | entities.remove(uuid) != null
            | tempUuids.remove(uuid)
            | tempEntities.remove(uuid) != null;

        if (executeRemoveAction && result) {
            actionOnRemove.ifPresent(action -> action.execute(getHolder(), entity));
        }

        return result;

    }

    public boolean contains(Entity entity) {
        return entities.containsValue(entity) || entityUuids.contains(entity.getUuid());
    }

    public int size() {
        return entityUuids.size();
    }

    public void clear() {

        if (actionOnRemove.isPresent()) {

            BiEntityAction actualActionOnRemove = actionOnRemove.get();

            for (UUID entityUuid : entityUuids) {
                actualActionOnRemove.execute(getHolder(), getEntity(entityUuid));
            }

        }

        boolean wasNotEmpty = !entityUuids.isEmpty() || !tempUuids.isEmpty();

        tempUuids.clear();
        tempEntities.clear();
        entityUuids.clear();
        entities.clear();

        if (wasNotEmpty) {
            PowerHolderComponent.syncPower(getHolder(), getPower());
        }

    }

    public Set<UUID> getIterationSet() {
        return new HashSet<>(entityUuids);
    }

    @Nullable
    public Entity getEntity(UUID uuid) {

        if (!entityUuids.contains(uuid)) {
            return null;
        }

        Entity entity = null;
        MinecraftServer server = getHolder().getServer();

        if (entities.containsKey(uuid)) {
            entity = entities.get(uuid);
        }

        if (server != null && (entity == null || entity.isRemoved())) {
            entity = MiscUtil.getEntityByUuid(uuid, server);
        }

        return entity;

    }

    @Override
    public NbtElement toTag() {

        NbtCompound rootNbt = new NbtCompound();

        NbtList entityUuidsNbt = new NbtList();
        NbtList tempUuidsNbt = new NbtList();

        for (UUID entityUuid : entityUuids) {
            NbtIntArray entityUuidNbt = NbtHelper.fromUuid(entityUuid);
            entityUuidsNbt.add(entityUuidNbt);
        }

        for (UUID tempUuid : tempUuids) {
            NbtIntArray tempUuidNbt = NbtHelper.fromUuid(tempUuid);
            tempUuidsNbt.add(tempUuidNbt);
        }

        rootNbt.put("Entities", entityUuidsNbt);
        rootNbt.put("TempEntities", tempUuidsNbt);
        rootNbt.putBoolean("RemovedTemps", removedTemps);

        return rootNbt;

    }

    @Override
    public void fromTag(NbtElement tag) {

        if (!(tag instanceof NbtCompound rootNbt)) {
            return;
        }

        tempUuids.clear();
        tempEntities.clear();
        entityUuids.clear();
        entities.clear();

        NbtList tempUuidsNbt = rootNbt.getList("TempEntities", NbtElement.INT_ARRAY_TYPE);
        for (NbtElement tempUuidNbt : tempUuidsNbt) {
            UUID tempUuid = NbtHelper.toUuid(tempUuidNbt);
            tempUuids.add(tempUuid);
        }

        NbtList entityUuidsNbt = rootNbt.getList("Entities", NbtElement.INT_ARRAY_TYPE);
        for (NbtElement entityUuidNbt : entityUuidsNbt) {
            UUID entityUuid = NbtHelper.toUuid(entityUuidNbt);
            entityUuids.add(entityUuid);
        }

        removedTemps = rootNbt.getBoolean("RemovedTemps");

    }

    public static void integrateLoadCallback(Entity loadedEntity, ServerWorld world) {
        PowerHolderComponent.syncPowers(loadedEntity, PowerHolderComponent.getPowerTypes(loadedEntity, EntitySetPowerType.class, true)
            .stream()
            .filter(Predicate.not(EntitySetPowerType::validateEntities))
            .map(PowerType::getPower)
            .toList());
    }

    public static void integrateUnloadCallback(Entity unloadedEntity, ServerWorld world) {

        Entity.RemovalReason removalReason = unloadedEntity.getRemovalReason();
        if (removalReason == null || !removalReason.shouldDestroy() || unloadedEntity instanceof PlayerEntity) {
            return;
        }

        for (ServerWorld otherWorld : world.getServer().getWorlds()) {

            for (Entity entity : otherWorld.iterateEntities()) {

                 PowerHolderComponent.syncPowers(entity, PowerHolderComponent.getPowerTypes(entity, EntitySetPowerType.class, true)
                    .stream()
                    .filter(p -> p.remove(unloadedEntity, false))
                    .map(PowerType::getPower)
                    .toList());

            }

        }

    }

}
