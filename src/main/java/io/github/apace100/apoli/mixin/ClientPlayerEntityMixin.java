package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import io.github.apace100.apoli.access.CustomToastViewer;
import io.github.apace100.apoli.access.WaterMovingEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.CustomToastData;
import io.github.apace100.apoli.power.type.*;
import io.github.apace100.apoli.screen.toast.CustomToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
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

import java.util.List;
import java.util.function.Predicate;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements WaterMovingEntity, CustomToastViewer {

    @Unique
    private boolean apoli$isMoving = false;

    @Shadow
    @Final
    protected MinecraftClient client;

    @Shadow
    protected abstract boolean isWalking();

    private ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(at = @At("HEAD"), method = "isSubmergedInWater", cancellable = true)
    private void allowSwimming(CallbackInfoReturnable<Boolean> cir)  {
        if(PowerHolderComponent.hasPowerType(this, SwimmingPowerType.class)) {
            cir.setReturnValue(true);
        } else if(PowerHolderComponent.hasPowerType(this, IgnoreWaterPowerType.class)) {
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
        return PowerHolderComponent.modify(this, ModifyAirSpeedPowerType.class, playerAbilities.getFlySpeed());
    }

    @Override
    public void apoli$showToast(CustomToastData toastData) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            CustomToast toast = new CustomToast(toastData);
            client.getToastManager().add(toast);
        });
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void apoli$cacheSprintingPowers(CallbackInfo ci, @Share("sprintingPowers") LocalRef<List<SprintingPowerType>> sprintingPowersRef, @Share("preventSprinting") LocalBooleanRef preventSprintingRef) {
        sprintingPowersRef.set(PowerHolderComponent.getPowerTypes(this, SprintingPowerType.class));
        preventSprintingRef.set(PowerHolderComponent.hasPowerType(this, PreventSprintingPowerType.class));
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;canStartSprinting()Z"))
    private boolean apoli$allowActivePowerSprinting(boolean original, @Share("sprintingPowers") LocalRef<List<SprintingPowerType>> sprintingPowersRef, @Share("preventSprinting") LocalBooleanRef preventSprintingRef) {
        return original || (this.isWalking() && sprintingPowersRef.get()
            .stream()
            .anyMatch(SprintingPowerType::shouldRequireInput));
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSprinting()Z"))
    private void apoli$allowPassivePowerSprinting(CallbackInfo ci, @Share("sprintingPowers") LocalRef<List<SprintingPowerType>> sprintingPowersRef, @Share("preventSprinting") LocalBooleanRef preventSprintingRef) {

        if (this.isSprinting() || preventSprintingRef.get()) {
            return;
        }

        this.setSprinting(sprintingPowersRef.get()
            .stream()
            .anyMatch(Predicate.not(SprintingPowerType::shouldRequireInput)));

    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;canSprint()Z"))
    private boolean apoli$accountForSprintingPowersWhenCancelling(boolean original, @Share("sprintingPowers") LocalRef<List<SprintingPowerType>> sprintingPowersRef, @Share("preventSprinting") LocalBooleanRef preventSprintingRef) {
        return (original || !sprintingPowersRef.get().isEmpty())
            && !preventSprintingRef.get();
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSneaking()Z"))
    private boolean apoli$forceSneakingPose(boolean original) {
        return original || PosePowerType.hasEntityPose(this, EntityPose.CROUCHING);
    }

}
