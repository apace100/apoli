package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.access.ModifiedPoseHolder;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.ArmPoseReference;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PosePowerType extends PowerType implements Prioritized<PosePowerType> {

    public static final TypedDataObjectFactory<PosePowerType> DATA_FACTORY = createConditionedDataFactory(
        new SerializableData()
            .add("entity_pose", ApoliDataTypes.ENTITY_POSE.optional(), Optional.empty())
            .add("arm_pose", ApoliDataTypes.ARM_POSE_REFERENCE.optional(), Optional.empty())
            .add("priority", SerializableDataTypes.INT, 0),
        (data, condition) -> new PosePowerType(
            data.get("entity_pose"),
            data.get("arm_pose"),
            data.get("priority"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_pose", powerType.getEntityPose())
            .set("arm_pose", powerType.getArmPose())
            .set("priority", powerType.getPriority())
    );

    private final Optional<EntityPose> entityPose;
    private final Optional<ArmPoseReference> armPose;

    private final int priority;

    public PosePowerType(Optional<EntityPose> entityPose, Optional<ArmPoseReference> armPose, int priority, Optional<EntityCondition> condition) {
        super(condition);
        this.entityPose = entityPose;
        this.armPose = armPose;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.POSE;
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

    public Optional<EntityPose> getEntityPose() {
        return entityPose;
    }

    public Optional<ArmPoseReference> getArmPose() {
        return armPose;
    }

    public boolean hasPose() {
        return this.getEntityPose().isPresent()
            || this.getArmPose().isPresent();
    }

    public static boolean hasEntityPose(Entity entity, EntityPose entityPose) {
        return entity instanceof ModifiedPoseHolder poseHolder
            && poseHolder.apoli$getModifiedEntityPose().map(entityPose::equals).orElse(false);
    }

}
