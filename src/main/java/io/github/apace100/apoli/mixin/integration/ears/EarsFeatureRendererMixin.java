package io.github.apace100.apoli.mixin.integration.ears;

import com.unascribed.ears.common.render.EarsRenderDelegate;
import com.unascribed.ears.common.render.IndirectEarsRenderDelegate;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.InvisibilityPowerType;
import io.github.apace100.apoli.power.type.ModelColorPowerType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "com.unascribed.ears.EarsFeatureRenderer$1")
public abstract class EarsFeatureRendererMixin extends IndirectEarsRenderDelegate<MatrixStack, VertexConsumerProvider, VertexConsumer, AbstractClientPlayerEntity, ModelPart>  {
    @Shadow(remap = false) protected abstract EquipmentSlot getSlot(EarsRenderDelegate.TexSource par1);

    @Shadow(remap = false) private float armorR;

    @Shadow(remap = false) private float armorG;

    @Shadow(remap = false) private float armorB;

    @Shadow(remap = false) private float armorA;

    @Inject(method = "getVertexConsumer(Lcom/unascribed/ears/common/render/EarsRenderDelegate$TexSource;)Lnet/minecraft/client/render/VertexConsumer;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/SkinTextures;texture()Lnet/minecraft/util/Identifier;"))
    private void apoli$setEarsRGBA(TexSource src, CallbackInfoReturnable<Object> cir) {
        AbstractClientPlayerEntity entity = this.peer;
        EquipmentSlot slot = this.getSlot(src);
        if (PowerHolderComponent.hasPowerType(entity, InvisibilityPowerType.class, p -> p.doesApply(MinecraftClient.getInstance().player) && (slot == null || !p.shouldRenderArmor()))) {
            this.armorA = 0.0F;
        } else if (slot == null) {
            List<ModelColorPowerType> modelColorPowers = PowerHolderComponent.getPowerTypes(entity, ModelColorPowerType.class);
            if (!modelColorPowers.isEmpty()) {
                //  TODO: Implement custom blending modes for blending colors -eggohito
                float newRed = modelColorPowers
                        .stream()
                        .map(ModelColorPowerType::getRed)
                        .reduce(this.armorR, (a, b) -> a * b);
                float newGreen = modelColorPowers
                        .stream()
                        .map(ModelColorPowerType::getGreen)
                        .reduce(this.armorG, (a, b) -> a * b);
                float newBlue = modelColorPowers
                        .stream()
                        .map(ModelColorPowerType::getBlue)
                        .reduce(this.armorB, (a, b) -> a * b);

                float oldAlpha = this.armorA;
                float newAlpha = modelColorPowers
                        .stream()
                        .map(ModelColorPowerType::getAlpha)
                        .min(Float::compareTo)
                        .map(alphaFactor -> oldAlpha * alphaFactor)
                        .orElse(oldAlpha);

                this.armorR *= newRed;
                this.armorG *= newGreen;
                this.armorB *= newBlue;
                this.armorA *= newAlpha;
            }
        }
    }
}
