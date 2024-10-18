package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

import java.util.Optional;

public class SpawnEntityEntityActionType extends EntityActionType {

    public static final DataObjectFactory<SpawnEntityEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("tag", SerializableDataTypes.NBT_COMPOUND, new NbtCompound()),
        data -> new SpawnEntityEntityActionType(
            data.get("entity_type"),
            data.get("entity_action"),
            data.get("bientity_action"),
            data.get("tag")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("entity_type", actionType.entityType)
            .set("entity_action", actionType.entityAction)
            .set("bientity_action", actionType.biEntityAction)
            .set("tag", actionType.tag)
    );

    private final EntityType<?> entityType;

    private final Optional<EntityAction> entityAction;
    private final Optional<BiEntityAction> biEntityAction;

    private final NbtCompound tag;

    public SpawnEntityEntityActionType(EntityType<?> entityType, Optional<EntityAction> entityAction, Optional<BiEntityAction> biEntityAction, NbtCompound tag) {
        this.entityType = entityType;
        this.entityAction = entityAction;
        this.biEntityAction = biEntityAction;
        this.tag = tag;
    }

    @Override
    protected void execute(Entity entity) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        Optional<Entity> entityToSpawn = MiscUtil.getEntityWithPassengers(
            serverWorld,
            entityType,
            tag,
            entity.getPos(),
            entity.getYaw(),
            entity.getPitch()
        );

        if (entityToSpawn.isEmpty()) {
            return;
        }

        Entity actualEntityToSpawn = entityToSpawn.get();
        serverWorld.spawnNewEntityAndPassengers(actualEntityToSpawn);

        entityAction.ifPresent(action -> action.execute(actualEntityToSpawn));
        biEntityAction.ifPresent(action -> action.execute(entity, actualEntityToSpawn));

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.SPAWN_ENTITY;
    }

}
