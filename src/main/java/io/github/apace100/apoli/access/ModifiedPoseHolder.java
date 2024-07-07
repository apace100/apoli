package io.github.apace100.apoli.access;

import io.github.apace100.apoli.util.ArmPoseReference;
import net.minecraft.entity.EntityPose;

public interface ModifiedPoseHolder {

    EntityPose apoli$getModifiedEntityPose();
    void apoli$setModifiedEntityPose(EntityPose entityPose);

    ArmPoseReference apoli$getModifiedArmPose();
    void apoli$setModifiedArmPose(ArmPoseReference armPose);

}
