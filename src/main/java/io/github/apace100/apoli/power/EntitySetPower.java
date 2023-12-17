package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EntitySetPower extends Power {

    private final Consumer<Pair<Entity, Entity>> actionOnAdd;
    private final Consumer<Pair<Entity, Entity>> actionOnRemove;
    private final int tickRate;

    private final Set<UUID> entityUuids = new HashSet<>();
    private final Map<UUID, Entity> entities = new HashMap<>();

    private final Map<UUID, Integer> tempEntities = new ConcurrentHashMap<>();

    public EntitySetPower(PowerType<?> type, LivingEntity entity, Consumer<Pair<Entity, Entity>> actionOnAdd, Consumer<Pair<Entity, Entity>> actionOnRemove, int tickRate) {
        super(type, entity);
        this.actionOnAdd = actionOnAdd;
        this.actionOnRemove = actionOnRemove;
        this.tickRate = tickRate;
        this.setTicking(true);
    }

    @Override
    public void tick() {
        tickTempEntities();
        validateEntities();
    }

    private void tickTempEntities() {

        if (tempEntities.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Integer>> entryIterator = tempEntities.entrySet().iterator();
        boolean shouldSync = false;

        while (entryIterator.hasNext()) {

            Map.Entry<UUID, Integer> entry = entryIterator.next();
            entry.setValue(entry.getValue() - 1);

            if (entry.getValue() <= 0 && this.remove(entry.getKey(), true, true)) {
                shouldSync = true;
            }

        }

        if (shouldSync) {
            PowerHolderComponent.syncPower(this.entity, this.type);
        }

    }

    private void validateEntities() {

        MinecraftServer server = entity.getServer();
        if (server == null || entity.age % tickRate != 0) {
            return;
        }

        Iterator<UUID> iterator = entityUuids.iterator();
        boolean shouldSync = false;

        while (iterator.hasNext()) {

            UUID uuid = iterator.next();
            if (MiscUtil.getEntityByUuid(uuid, server) != null) {
                continue;
            }

            this.remove(uuid, false, false);
            shouldSync = true;

        }

        if (shouldSync) {
            PowerHolderComponent.syncPower(this.entity, this.type);
        }

    }

    public boolean add(Entity entity) {
        return add(entity, null);
    }

    public boolean add(Entity entity, @Nullable Integer time) {

        if (entity == null || entity.isRemoved() || entity.getWorld().isClient || (time != null && time <= 0)) {
            return false;
        }

        if (time != null) {
            tempEntities.compute(entity.getUuid(), (prevUuid, prevTime) -> time);
        }

        UUID uuid = entity.getUuid();
        if (entityUuids.contains(uuid)) {
            return false;
        }

        entityUuids.add(uuid);
        entities.put(uuid, entity);

        if (actionOnAdd != null) {
            actionOnAdd.accept(new Pair<>(this.entity, entity));
        }

        PowerHolderComponent.syncPower(this.entity, this.type);
        return true;

    }

    public boolean remove(Entity entity, boolean shouldSync) {

        if (entity != null && !entity.getWorld().isClient) {
            return remove(entity.getUuid(), true, shouldSync);
        }

        return false;

    }

    private boolean remove(UUID uuid, boolean executeActions, boolean shouldSync) {

        if (!entityUuids.remove(uuid) | entities.remove(uuid) == null | tempEntities.remove(uuid) == null) {
            return false;
        }

        if (executeActions && actionOnRemove != null) {
            actionOnRemove.accept(new Pair<>(entity, getEntity(uuid)));
        }

        if (shouldSync) {
            PowerHolderComponent.syncPower(this.entity, this.type);
        }

        return true;

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
                actionOnRemove.accept(new Pair<>(this.entity, getEntity(entityUuid)));
            }

        }

        int prevEntityUuidsSize = entityUuids.size();

        tempEntities.clear();
        entityUuids.clear();
        entities.clear();

        if (prevEntityUuidsSize > 0) {
            PowerHolderComponent.syncPower(this.entity, this.type);
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

        if (entity == null || entity.isRemoved()) {
            this.remove(uuid, false, true);
        }

        return entity;

    }

    @Override
    public NbtElement toTag() {

        NbtCompound rootNbt = new NbtCompound();
        NbtList entityUuidsNbt = new NbtList();

        for (UUID entityUuid : entityUuids) {
            NbtIntArray entityUuidNbt = NbtHelper.fromUuid(entityUuid);
            entityUuidsNbt.add(entityUuidNbt);
        }

        rootNbt.put("Entities", entityUuidsNbt);
        return rootNbt;

    }

    @Override
    public void fromTag(NbtElement tag) {

        if (!(tag instanceof NbtCompound rootNbt)) {
            return;
        }

        entityUuids.clear();
        entities.clear();

        NbtList entityUuidsNbt = rootNbt.getList("Entities", NbtElement.INT_ARRAY_TYPE);
        for (NbtElement entityUuidNbt : entityUuidsNbt) {
            UUID entityUuid = NbtHelper.toUuid(entityUuidNbt);
            entityUuids.add(entityUuid);
        }

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("entity_set"),
            new SerializableData()
                .add("action_on_add", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("action_on_remove", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("tick_rate", SerializableDataTypes.POSITIVE_INT, 100),
            data -> (powerType, livingEntity) -> new EntitySetPower(
                powerType,
                livingEntity,
                data.get("action_on_add"),
                data.get("action_on_remove"),
                data.get("tick_rate")
            )
        );
    }

}
