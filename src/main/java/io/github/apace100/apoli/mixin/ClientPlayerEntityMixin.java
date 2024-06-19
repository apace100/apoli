package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import io.github.apace100.apoli.access.CustomToastViewer;
import io.github.apace100.apoli.access.WaterMovingEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.CustomToastData;
import io.github.apace100.apoli.power.IgnoreWaterPower;
import io.github.apace100.apoli.power.ModifyAirSpeedPower;
import io.github.apace100.apoli.power.PreventSprintingPower;
import io.github.apace100.apoli.power.SprintingPower;
import io.github.apace100.apoli.power.SwimmingPower;
import io.github.apace100.apoli.screen.toast.CustomToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerAbilities;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements WaterMovingEntity, CustomToastViewer {

    @Unique
    private boolean apoli$isMoving = false;

    @Shadow @Final protected MinecraftClient client;

    @Shadow protected int ticksLeftToDoubleTapSprint;

    @Shadow protected abstract boolean isWalking();

    @Shadow public Input input;

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
    public void apoli$showToast(CustomToastData toastData) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            CustomToast toast = new CustomToast(toastData);
            client.getToastManager().add(toast);
        });
    }

    @Unique
    private boolean apoli$previousWalkingState = false;

    @ModifyVariable(method = "tickMovement", at = @At(value = "STORE", ordinal = 0), ordinal = 4)
    private boolean apoli$allowDoubleTapSprint(boolean original) {
        return original || PowerHolderComponent.getPowers(this, SprintingPower.class).stream().anyMatch(SprintingPower::shouldRequireInput) && isWalking() && this.ticksLeftToDoubleTapSprint <= 0 && !this.client.options.sprintKey.isPressed();
    }

    @ModifyVariable(method = "tickMovement", at = @At(value = "STORE", ordinal = 0), ordinal = 5)
    private boolean apoli$allowDoubleTapSprint2(boolean original) {
        return original || PowerHolderComponent.getPowers(this, SprintingPower.class).stream().anyMatch(SprintingPower::shouldRequireInput) && isWalking() && this.ticksLeftToDoubleTapSprint <= 0 && !this.client.options.sprintKey.isPressed();
    }

    @ModifyVariable(method = "tickMovement", at = @At(value = "STORE", ordinal = 0), ordinal = 6)
    private boolean apoli$allowDoubleTapSprint3(boolean original) {
        boolean setWalkingState = PowerHolderComponent.getPowers(this, SprintingPower.class).stream().anyMatch(SprintingPower::shouldRequireInput) && isWalking() && this.ticksLeftToDoubleTapSprint <= 0 && !this.client.options.sprintKey.isPressed();
        if (setWalkingState) {
            this.apoli$previousWalkingState = true;
        }
        return original || setWalkingState;
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSprinting()Z"))
    private void apugli$allowPowerSprinting(CallbackInfo ci) {
        if (PowerHolderComponent.hasPower(this, SprintingPower.class) && !this.isSprinting() && PowerHolderComponent.getPowers(this, SprintingPower.class).stream().noneMatch(SprintingPower::shouldRequireInput) || this.client.options.sprintKey.isPressed() || this.isWalking() && !this.apoli$previousWalkingState && this.ticksLeftToDoubleTapSprint > 0) {
            this.setSprinting(true);
        }
        if (apoli$previousWalkingState && !this.isWalking()) {
            this.apoli$previousWalkingState = false;
        }
    }

    @ModifyVariable(method = "tickMovement", at = @At(value = "STORE", ordinal = 1), ordinal = 4)
    private boolean apugli$cancelOutWaterSprintFalse(boolean value) {
        return value && (PowerHolderComponent.getPowers(this, SprintingPower.class).stream().noneMatch(SprintingPower::shouldRequireInput) || !this.input.hasForwardMovement());
    }

    @ModifyVariable(method = "tickMovement", at = @At(value = "STORE", ordinal = 1), ordinal = 5)
    private boolean apugli$resetPowerSprinting(boolean value) {
        if (PowerHolderComponent.hasPower(this, SprintingPower.class)) {
            return PowerHolderComponent.getPowers(this, SprintingPower.class).stream().anyMatch(SprintingPower::shouldRequireInput) && (!this.isWalking() || this.horizontalCollision && !this.collidedSoftly);
        }
        return value;
    }
}
