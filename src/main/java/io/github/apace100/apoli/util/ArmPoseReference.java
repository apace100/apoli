package io.github.apace100.apoli.util;

import io.github.apace100.apoli.access.ModifiedPoseHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.Entity;

import java.util.Optional;

public enum ArmPoseReference {

    EMPTY,
    ITEM,
    BLOCK,
    BOW_AND_ARROW,
    THROW_SPEAR,
    CROSSBOW_CHARGE,
    CROSSBOW_HOLD,
    SPYGLASS,
    TOOT_HORN,
    BRUSH;

    @Environment(EnvType.CLIENT)
    public static Optional<BipedEntityModel.ArmPose> getArmPose(Entity entity) {

        if (!(entity instanceof ModifiedPoseHolder poseHolder) || poseHolder.apoli$getModifiedArmPose() == null) {
            return Optional.empty();
        }

        return Optional.of(switch (poseHolder.apoli$getModifiedArmPose()) {
            case EMPTY ->
                BipedEntityModel.ArmPose.EMPTY;
            case ITEM ->
                BipedEntityModel.ArmPose.ITEM;
            case BLOCK ->
                BipedEntityModel.ArmPose.BLOCK;
            case BRUSH ->
                BipedEntityModel.ArmPose.BRUSH;
            case SPYGLASS ->
                BipedEntityModel.ArmPose.SPYGLASS;
            case TOOT_HORN ->
                BipedEntityModel.ArmPose.TOOT_HORN;
            case THROW_SPEAR ->
                BipedEntityModel.ArmPose.THROW_SPEAR;
            case BOW_AND_ARROW ->
                BipedEntityModel.ArmPose.BOW_AND_ARROW;
            case CROSSBOW_HOLD ->
                BipedEntityModel.ArmPose.CROSSBOW_HOLD;
            case CROSSBOW_CHARGE ->
                BipedEntityModel.ArmPose.CROSSBOW_CHARGE;
        });

    }

}
