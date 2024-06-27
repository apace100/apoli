package io.github.apace100.apoli.access;

import io.github.apace100.apoli.util.ApoliArmPose;
import net.minecraft.entity.EntityPose;

public interface ModifiedPoseHolder {

    EntityPose apoli$getModifiedEntityPose();
    void apoli$setModifiedEntityPose(EntityPose entityPose);

    ApoliArmPose apoli$getModifiedArmPose();
    void apoli$setModifiedArmPose(ApoliArmPose armPose);

}
