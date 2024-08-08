package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntitySetPowerType extends PowerType {

    private final Consumer<Pair<Entity, Entity>> actionOnAdd;
    private final Consumer<Pair<Entity, Entity>> actionOnRemove;
    private final int tickRate;

    private final Set<UUID> entityUuids = new HashSet<>();
    private final Map<UUID, Entity> entities = new HashMap<>();

    private final Set<UUID> tempUuids = new HashSet<>();
    private final Map<UUID, Long> tempEntities = new ConcurrentHashMap<>();

    private Integer startTicks = null;

    private boolean wasActive = false;
    private boolean removedTemps = false;

    public EntitySetPowerType(Power power, LivingEntity entity, Consumer<Pair<Entity, Entity>> actionOnAdd, Consumer<Pair<Entity, Entity>> actionOnRemove, int tickRate) {
        super(power, entity);
        this.actionOnAdd = actionOnAdd;
        this.actionOnRemove = actionOnRemove;
        this.tickRate = tickRate;
        this.setTicking(true);
    }

    @Override
    public void onAdded() {
        removedTemps = entityUuids.removeIf(tempUuids::contains);
        tempUuids.clear();
    }

    @Override
    public void tick() {

        if (removedTemps) {

            this.removedTemps = false;
            PowerHolderComponent.syncPower(this.entity, this.power);

            return;

        }

        if (!tempEntities.isEmpty() && this.isActive()) {

            if (startTicks == null) {
                this.startTicks = entity.age % tickRate;
                return;
            }

            if (entity.age % tickRate == startTicks) {
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
        long time = entity.getWorld().getTime();

        while (entryIterator.hasNext()) {

            Map.Entry<UUID, Long> entry = entryIterator.next();
            if (time < entry.getValue()) {
                continue;
            }

            UUID uuid = entry.getKey();
            Entity tempEntity = this.getEntity(uuid);

            entryIterator.remove();
            if (entityUuids.remove(uuid) | entities.remove(uuid) != null | tempUuids.remove(uuid)) {

                if (actionOnRemove != null) {
                    actionOnRemove.accept(new Pair<>(entity, tempEntity));
                }

                this.removedTemps = true;

            }

        }

    }

    public boolean validateEntities() {

        MinecraftServer server = entity.getServer();
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
        return add(entity, null);
    }

    public boolean add(Entity entity, @Nullable Integer time) {

        if (entity == null || entity.isRemoved() || entity.getWorld().isClient) {
            return false;
        }

        UUID uuid = entity.getUuid();
        boolean addedToSet = false;

        if (time != null) {
            addedToSet |= tempUuids.add(uuid);
            tempEntities.compute(uuid, (prevUuid, prevTime) -> entity.getWorld().getTime() + time);
        }

        if (!entityUuids.contains(uuid)) {

            addedToSet |= entityUuids.add(uuid);
            entities.put(uuid, entity);

            if (actionOnAdd != null) {
                actionOnAdd.accept(new Pair<>(this.entity, entity));
            }

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

        if (executeRemoveAction && result && actionOnRemove != null) {
            actionOnRemove.accept(new Pair<>(this.entity, entity));
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

        if (actionOnRemove != null) {

            for (UUID entityUuid : entityUuids) {
                actionOnRemove.accept(new Pair<>(this.entity, this.getEntity(entityUuid)));
            }

        }

        boolean wasNotEmpty = !entityUuids.isEmpty() || !tempUuids.isEmpty();

        tempUuids.clear();
        tempEntities.clear();
        entityUuids.clear();
        entities.clear();

        if (wasNotEmpty) {
            PowerHolderComponent.syncPower(this.entity, this.power);
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
        MinecraftServer server = this.entity.getServer();

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

    public static PowerTypeFactory<EntitySetPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("entity_set"),
            new SerializableData()
                .add("action_on_add", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("action_on_remove", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("tick_rate", SerializableDataTypes.POSITIVE_INT, 1),
            data -> (power, entity) -> new EntitySetPowerType(
                power,
                entity,
                data.get("action_on_add"),
                data.get("action_on_remove"),
                data.get("tick_rate")
            )
        ).allowCondition();
    }

}
