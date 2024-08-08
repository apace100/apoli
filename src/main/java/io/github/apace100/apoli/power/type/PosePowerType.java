package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.ModifiedPoseHolder;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.ArmPoseReference;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class PosePowerType extends PowerType implements Prioritized<PosePowerType> {

    @Nullable
    private final EntityPose entityPose;

    @Nullable
    private final ArmPoseReference armPose;

    private final int priority;

    public PosePowerType(Power power, LivingEntity entity, @Nullable EntityPose entityPose, @Nullable ArmPoseReference armPose, int priority) {
        super(power, entity);
        this.entityPose = entityPose;
        this.armPose = armPose;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isActive() {
        return this.hasPose()
            && super.isActive();
    }

    @Nullable
    public EntityPose getEntityPose() {
        return entityPose;
    }

    @Nullable
    public ArmPoseReference getArmPose() {
        return armPose;
    }

    public boolean hasPose() {
        return this.getEntityPose() != null
            || this.getArmPose() != null;
    }

    public static boolean hasEntityPose(Entity entity, EntityPose entityPose) {
        return entity instanceof ModifiedPoseHolder poseHolder
            && poseHolder.apoli$getModifiedEntityPose() == entityPose;
    }

    public static PowerTypeFactory<PosePowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("pose"),
            new SerializableData()
                .add("entity_pose", ApoliDataTypes.ENTITY_POSE, null)
                .add("arm_pose", ApoliDataTypes.ARM_POSE_REFERENCE, null)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new PosePowerType(power, entity,
                data.get("entity_pose"),
                data.get("arm_pose"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
