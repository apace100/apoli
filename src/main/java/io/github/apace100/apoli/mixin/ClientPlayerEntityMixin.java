package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import io.github.apace100.apoli.access.ToastViewer;
import io.github.apace100.apoli.access.WaterMovingEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.DynamicToastData;
import io.github.apace100.apoli.power.IgnoreWaterPower;
import io.github.apace100.apoli.power.ModifyAirSpeedPower;
import io.github.apace100.apoli.power.PreventSprintingPower;
import io.github.apace100.apoli.power.SwimmingPower;
import io.github.apace100.apoli.screen.toast.DynamicToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerAbilities;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements WaterMovingEntity, ToastViewer {

    @Unique
    private boolean apoli$isMoving = false;

    @Shadow
    @Final
    public ClientPlayNetworkHandler networkHandler;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(at = @At("HEAD"), method = "isSubmergedInWater", cancellable = true)
    private void allowSwimming(CallbackInfoReturnable<Boolean> cir)  {
        if(PowerHolderComponent.hasPower(this, SwimmingPower.class)) {
            cir.setReturnValue(true);
        } else if(PowerHolderComponent.hasPower(this, IgnoreWaterPower.class)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(at = @At("HEAD"), method = "tickMovement")
    private void beginMovementPhase(CallbackInfo ci) {
        apoli$isMoving = true;
    }

    @Inject(at = @At("TAIL"), method = "tickMovement")
    private void endMovementPhase(CallbackInfo ci) {
        apoli$isMoving = false;
    }

    @Override
    public boolean apoli$isInMovementPhase() {
        return apoli$isMoving;
    }

    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerAbilities;getFlySpeed()F"))
    private float modifyFlySpeed(PlayerAbilities playerAbilities){
        return PowerHolderComponent.modify(this, ModifyAirSpeedPower.class, playerAbilities.getFlySpeed());
    }

    @ModifyReturnValue(method = "canSprint", at = @At("RETURN"))
    private boolean apoli$preventSprinting(boolean original) {
        return !PowerHolderComponent.hasPower(this, PreventSprintingPower.class) && original;
    }

    @Override
    public void apoli$showToast(DynamicToastData toastData) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            DynamicToast toast = new DynamicToast(toastData);
            client.getToastManager().add(toast);
        });
    }

}
