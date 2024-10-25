package io.github.apace100.apoli.access;

import io.github.apace100.apoli.util.ArmPoseReference;
import net.minecraft.entity.EntityPose;

import java.util.Optional;

public interface ModifiedPoseHolder {

    Optional<EntityPose> apoli$getModifiedEntityPose();
    void apoli$setModifiedEntityPose(EntityPose entityPose);

    Optional<ArmPoseReference> apoli$getModifiedArmPose();
    void apoli$setModifiedArmPose(ArmPoseReference armPose);

}
