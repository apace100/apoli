package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PseudoRenderDataHolder;
import io.github.apace100.apoli.power.PosePower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class EntityMixinClient implements PseudoRenderDataHolder {

    @Unique
    private int apoli$pseudoDeathTicks;

    @Unique
    private int apoli$pseudoFallFlyingTicks;

    @Override
    public int apoli$getPseudoDeathTicks() {
        return apoli$pseudoDeathTicks;
    }

    @Override
    public int apoli$getPseudoFallFlyingTicks() {
        return apoli$pseudoFallFlyingTicks;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void apoli$tickPseudoVars(CallbackInfo ci) {

        Entity thisAsEntity = (Entity) (Object) this;
        if (PosePower.hasEntityPose(thisAsEntity, EntityPose.DYING)) {
            ++this.apoli$pseudoDeathTicks;
        }

        else  {
            this.apoli$pseudoDeathTicks = 0;
        }

        if (PosePower.hasEntityPose(thisAsEntity, EntityPose.FALL_FLYING)) {
            ++this.apoli$pseudoFallFlyingTicks;
        }

        else {
            this.apoli$pseudoFallFlyingTicks = 0;
        }

    }

}
