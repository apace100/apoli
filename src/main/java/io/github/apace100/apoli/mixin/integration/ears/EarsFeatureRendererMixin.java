package io.github.apace100.apoli.mixin.integration.ears;

import com.unascribed.ears.common.render.EarsRenderDelegate;
import com.unascribed.ears.common.render.IndirectEarsRenderDelegate;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.InvisibilityPower;
import io.github.apace100.apoli.power.ModelColorPower;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(targets = "com.unascribed.ears.EarsFeatureRenderer$1", remap = false)
public abstract class EarsFeatureRendererMixin extends IndirectEarsRenderDelegate<MatrixStack, VertexConsumerProvider, VertexConsumer, AbstractClientPlayerEntity, ModelPart>  {
    @Shadow(remap = false) protected abstract EquipmentSlot getSlot(EarsRenderDelegate.TexSource par1);

    @Shadow(remap = false) private float armorR;

    @Shadow(remap = false) private float armorG;

    @Shadow(remap = false) private float armorB;

    @Shadow(remap = false) private float armorA;

    @Inject(method = "getVertexConsumer(Lcom/unascribed/ears/common/render/EarsRenderDelegate$TexSource;)Ljava/lang/Object;", at = @At("RETURN"), remap = false)
    private void apoli$setRGBA(TexSource src, CallbackInfoReturnable<Object> cir) {
        AbstractClientPlayerEntity entity = this.peer;
        EquipmentSlot slot = this.getSlot(src);
        if ((slot == null || PowerHolderComponent.hasPower(entity, InvisibilityPower.class, Predicate.not(InvisibilityPower::shouldRenderArmor)))) {
            List<ModelColorPower> modelColorPowers = PowerHolderComponent.getPowers(entity, ModelColorPower.class);
            if (!modelColorPowers.isEmpty()) {
                float re = modelColorPowers.stream().map(ModelColorPower::getRed).reduce((a, b) -> a * b).orElse(1.0f);
                float gr = modelColorPowers.stream().map(ModelColorPower::getGreen).reduce((a, b) -> a * b).orElse(1.0f);
                float bl = modelColorPowers.stream().map(ModelColorPower::getBlue).reduce((a, b) -> a * b).orElse(1.0f);
                float al = modelColorPowers.stream().map(ModelColorPower::getAlpha).reduce((a, b) -> a * b).orElse(1.0f);

                this.armorR *= re;
                this.armorG *= gr;
                this.armorB *= bl;
                this.armorA *= al;
            } else if (PowerHolderComponent.hasPower(entity, InvisibilityPower.class, p -> p.doesApply(MinecraftClient.getInstance().player) && (slot == null || p.shouldRenderArmor()))) {
                this.armorA = 0.0F;
            }
        }
    }
}
