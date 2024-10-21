package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

public class SpawnEffectCloudEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<SpawnEffectCloudEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("effect_component", SerializableDataTypes.POTION_CONTENTS_COMPONENT, PotionContentsComponent.DEFAULT)
            .add("wait_time", SerializableDataTypes.INT, 10)
            .add("radius", SerializableDataTypes.FLOAT, 3.0F)
            .add("radius_on_use", SerializableDataTypes.FLOAT, -0.5F)
            .add("duration", SerializableDataTypes.INT, 600)
            .add("duration_on_use", SerializableDataTypes.INT, 0),
        data -> new SpawnEffectCloudEntityActionType(
            data.get("effect_component"),
            data.get("wait_time"),
            data.get("radius"),
            data.get("radius_on_use"),
            data.get("duration"),
            data.get("duration_on_use")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("effect_component", actionType.effectComponent)
            .set("wait_time", actionType.waitTime)
            .set("radius", actionType.radius)
            .set("radius_on_use", actionType.radiusOnUse)
            .set("duration", actionType.duration)
            .set("duration_on_use", actionType.durationOnUse)
    );

    private final PotionContentsComponent effectComponent;
    private final int waitTime;

    private final float radius;
    private final float radiusOnUse;

    private final int duration;
    private final int durationOnUse;

    public SpawnEffectCloudEntityActionType(PotionContentsComponent effectComponent, int waitTime, float radius, float radiusOnUse, int duration, int durationOnUse) {
        this.effectComponent = effectComponent;
        this.waitTime = waitTime;
        this.radius = radius;
        this.radiusOnUse = radiusOnUse;
        this.duration = duration;
        this.durationOnUse = durationOnUse;
    }

    @Override
    protected void execute(Entity entity) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        AreaEffectCloudEntity aec = new AreaEffectCloudEntity(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ());
        if (entity instanceof LivingEntity living) {
            aec.setOwner(living);
        }

        aec.setPotionContents(effectComponent);
        aec.setRadius(radius);
        aec.setRadiusOnUse(radiusOnUse);
        aec.setDuration(duration);
        aec.setDurationOnUse(durationOnUse);
        aec.setWaitTime(waitTime);

        serverWorld.spawnEntity(aec);

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.SPAWN_EFFECT_CLOUD;
    }

}
