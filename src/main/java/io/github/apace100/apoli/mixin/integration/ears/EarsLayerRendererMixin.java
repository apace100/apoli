package io.github.apace100.apoli.mixin.integration.ears;

import com.unascribed.ears.common.render.AbstractEarsRenderDelegate;
import com.unascribed.ears.common.render.EarsRenderDelegate;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModelColorPower;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "com.unascribed.ears.EarsFeatureRenderer$1")
public abstract class EarsLayerRendererMixin {
    @Shadow(remap = false) private float armorR;
    @Shadow(remap = false) private float armorG;
    @Shadow(remap = false) private float armorB;
    @Shadow(remap = false) private float armorA;
    @Shadow(remap = false) public abstract boolean canBind(EarsRenderDelegate.TexSource par1);


    @Shadow protected abstract EquipmentSlot getSlot(EarsRenderDelegate.TexSource par1);

    @Inject(method = "getVertexConsumer(Lcom/unascribed/ears/common/render/EarsRenderDelegate$TexSource;)Lnet/minecraft/client/render/VertexConsumer;", at = @At("RETURN"))
    public void setRGBA(EarsRenderDelegate.TexSource src, CallbackInfoReturnable<VertexConsumer> cir) {
        if (this.getSlot(src) == null) {
            AbstractClientPlayerEntity entity = (AbstractClientPlayerEntity) ((AbstractEarsRenderDelegate)(Object)this).getPeer();
            List<ModelColorPower> modelColorPowers = PowerHolderComponent.getPowers(entity, ModelColorPower.class);
            if (!modelColorPowers.isEmpty()) {
                float red = modelColorPowers.stream().map(ModelColorPower::getRed).reduce((a, b) -> a * b).orElse(1.0f);
                float green = modelColorPowers.stream().map(ModelColorPower::getGreen).reduce((a, b) -> a * b).orElse(1.0f);
                float blue = modelColorPowers.stream().map(ModelColorPower::getBlue).reduce((a, b) -> a * b).orElse(1.0f);
                float alpha = modelColorPowers.stream().map(ModelColorPower::getAlpha).reduce((a, b) -> a * b).orElse(1.0f);

                this.armorR *= red;
                this.armorG *= green;
                this.armorB *= blue;
                this.armorA *= alpha;
            }
        }
    }
}
