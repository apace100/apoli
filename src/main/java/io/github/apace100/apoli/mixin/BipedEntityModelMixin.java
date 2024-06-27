package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.access.ModifiedPoseHolder;
import io.github.apace100.apoli.util.ApoliArmPose;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AnimalModel<T> implements ModelWithArms, ModelWithHead {

    @ModifyExpressionValue(method = "positionRightArm", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;rightArmPose:Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;"))
    private BipedEntityModel.ArmPose apoli$overrideRightArmPose(BipedEntityModel.ArmPose original, T entity) {
        return entity instanceof ModifiedPoseHolder poseHolder
            ? ApoliArmPose.convertOrOriginal(poseHolder.apoli$getModifiedArmPose(), original)
            : original;
    }

    @ModifyExpressionValue(method = "positionLeftArm", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;leftArmPose:Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;"))
    private BipedEntityModel.ArmPose apoli$overrideLeftArmPose(BipedEntityModel.ArmPose original, T entity) {
        return entity instanceof ModifiedPoseHolder poseHolder
            ? ApoliArmPose.convertOrOriginal(poseHolder.apoli$getModifiedArmPose(), original)
            : original;
    }

}
