package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.ModifiedPoseHolder;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class EntityPosePower extends Power implements Prioritized<EntityPosePower> {

    private final EntityPose pose;
    private final int priority;

    public EntityPosePower(PowerType<?> type, LivingEntity entity, EntityPose pose, int priority) {
        super(type, entity);

        this.pose = pose;
        this.priority = priority;

        if (!(entity instanceof PlayerEntity)) {
            this.setTicking();
        }

    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void tick() {
        entity.setPose(pose);
    }

    public EntityPose getPose() {
        return pose;
    }

    public static boolean isPosed(Entity entity, EntityPose entityPose) {
        return entity instanceof ModifiedPoseHolder poseHolder
            && poseHolder.apoli$getModifiedEntityPose() == entityPose;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("entity_pose"),
            new SerializableData()
                .add("entity_pose", ApoliDataTypes.ENTITY_POSE)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (powerType, livingEntity) -> new EntityPosePower(
                powerType,
                livingEntity,
                data.get("entity_pose"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
