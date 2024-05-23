package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.access.JumpingEntity;
import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.packet.s2c.DismountPlayerS2CPacket;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.ActionResultUtil;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.PriorityPhase;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = PlayerEntity.class, priority = 999)
public abstract class PlayerEntityMixin extends LivingEntity implements Nameable, CommandOutput, JumpingEntity {

    @Shadow
    public abstract boolean damage(DamageSource source, float amount);

    @Shadow
    public abstract EntityDimensions getDimensions(EntityPose pose);

    @Shadow
    public abstract ItemStack getEquippedStack(EquipmentSlot slot);

    @Shadow
    @Final
    private PlayerInventory inventory;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getOffGroundSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyFlySpeed(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(PowerHolderComponent.modify(this, ModifyAirSpeedPower.class, cir.getReturnValue()));
    }

    @ModifyVariable(method = "eatFood", at = @At("HEAD"), argsOnly = true)
    private ItemStack apoli$modifyEatenStack(ItemStack original) {

        StackReference newStack = InventoryUtil.createStackReference(original);
        ModifiableFoodEntity modifiableFoodEntity = (ModifiableFoodEntity) this;

        List<ModifyFoodPower> modifyFoodPowers = PowerHolderComponent.getPowers(this, ModifyFoodPower.class)
            .stream()
            .filter(mfp -> mfp.doesApply(original))
            .toList();

        for (ModifyFoodPower modifyFoodPower : modifyFoodPowers) {
            modifyFoodPower.setConsumedItemStackReference(newStack);
        }

        EdibleItemPower.get(original.copy(), this).ifPresent(modifiableFoodEntity::apoli$setEdibleItemPower);

        modifiableFoodEntity.apoli$setCurrentModifyFoodPowers(modifyFoodPowers);
        modifiableFoodEntity.apoli$setOriginalFoodStack(original);

        return newStack.get();

    }

    @Inject(method = "damage", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
    private void allowDamageIfModifyingPowersExist(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        boolean hasModifyingPower = false;

        if (source.getAttacker() != null) {
            if (source.isIn(DamageTypeTags.IS_PROJECTILE)) hasModifyingPower = PowerHolderComponent.hasPower(source.getAttacker(), ModifyProjectileDamagePower.class, mpdp -> mpdp.doesApply(source, amount, this));
            else hasModifyingPower = PowerHolderComponent.hasPower(source.getAttacker(), ModifyDamageDealtPower.class, mddp -> mddp.doesApply(source, amount, this));
        }

        hasModifyingPower |= PowerHolderComponent.hasPower(this, ModifyDamageTakenPower.class, mdtp -> mdtp.doesApply(source, amount));
        if (hasModifyingPower) cir.setReturnValue(super.damage(source, amount));

    }

    @Inject(method = "dismountVehicle", at = @At("HEAD"))
    private void apoli$sendPlayerDismountPacket(CallbackInfo ci) {
        if (this.getVehicle() instanceof ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, new DismountPlayerS2CPacket(this.getId()));
        }
    }

    @Inject(method = "updateSwimming", at = @At("TAIL"))
    private void updateSwimmingPower(CallbackInfo ci) {
        if(PowerHolderComponent.hasPower(this, SwimmingPower.class)) {
            this.setSwimming(this.isSprinting() && !this.hasVehicle());
            this.touchingWater = this.isSwimming();
            if (this.isSwimming()) {
                this.fallDistance = 0.0F;
                Vec3d look = this.getRotationVector();
                move(MovementType.SELF, new Vec3d(look.x/4, look.y/4, look.z/4));
            }
        } else if(PowerHolderComponent.hasPower(this, IgnoreWaterPower.class)) {
            this.setSwimming(false);
        }
    }

    @Inject(method = "wakeUp(ZZ)V", at = @At("HEAD"))
    private void invokeWakeUpAction(boolean bl, boolean updateSleepingPlayers, CallbackInfo ci) {
        if(!bl && !updateSleepingPlayers && getSleepingPosition().isPresent()) {
            BlockPos sleepingPos = getSleepingPosition().get();
            PowerHolderComponent.getPowers(this, ActionOnWakeUp.class).stream().filter(p -> p.doesApply(sleepingPos)).forEach(p -> p.executeActions(sleepingPos, Direction.DOWN));
        }
    }

    // Prevent healing if DisableRegenPower
    // Note that this function was called "shouldHeal" instead of "canFoodHeal" at some point in time.
    @Inject(method = "canFoodHeal", at = @At("HEAD"), cancellable = true)
    private void disableHeal(CallbackInfoReturnable<Boolean> info) {
        if(PowerHolderComponent.hasPower(this, DisableRegenPower.class)) {
            info.setReturnValue(false);
        }
    }

    // ModifyExhaustion
    @ModifyVariable(at = @At("HEAD"), method = "addExhaustion", ordinal = 0, name = "exhaustion")
    private float modifyExhaustion(float exhaustionIn) {
        return PowerHolderComponent.modify(this, ModifyExhaustionPower.class, exhaustionIn);
    }

    @Inject(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;dropAll()V"))
    private void dropAdditionalInventory(CallbackInfo ci) {
        PowerHolderComponent.getPowers(this, InventoryPower.class).forEach(inventoryPower -> {
            if(inventoryPower.shouldDropOnDeath()) {
                inventoryPower.dropItemsOnDeath();
            }
        });
        PowerHolderComponent.getPowers(this, KeepInventoryPower.class).forEach(keepInventoryPower -> {
            keepInventoryPower.preventItemsFromDropping(inventory);
        });
    }

    @Inject(method = "dropInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;dropAll()V", shift = At.Shift.AFTER))
    private void restoreKeptInventory(CallbackInfo ci) {
        PowerHolderComponent.getPowers(this, KeepInventoryPower.class).forEach(keepInventoryPower -> {
            keepInventoryPower.restoreSavedItems(inventory);
        });
    }

    @Inject(method = "canEquip", at = @At("HEAD"), cancellable = true)
    private void preventArmorDispensing(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        EquipmentSlot slot = MobEntity.getPreferredEquipmentSlot(stack);
        PowerHolderComponent component = PowerHolderComponent.KEY.get(this);
        if(component.getPowers(RestrictArmorPower.class).stream().anyMatch(rap -> !rap.canEquip(stack, slot))) {
            info.setReturnValue(false);
        }
    }

    @WrapOperation(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult apoli$beforeEntityUse(Entity entity, PlayerEntity player, Hand hand, Operation<ActionResult> original, @Share("zeroPriority$onEntity") LocalRef<ActionResult> sharedZeroPriority$onEntity) {

        ItemStack stackInHand = player.getStackInHand(hand);
        for (PreventEntityUsePower peup : PowerHolderComponent.getPowers(this, PreventEntityUsePower.class)) {

            if (peup.doesApply(entity, hand, stackInHand)) {
                return peup.executeAction(entity, hand);
            }

        }

        for (PreventBeingUsedPower pbup : PowerHolderComponent.getPowers(entity, PreventBeingUsedPower.class)) {

            if (pbup.doesApply(player, hand, stackInHand)) {
                return pbup.executeAction(player, hand);
            }

        }

        Prioritized.CallInstance<ActiveInteractionPower> aipci = new Prioritized.CallInstance<>();

        aipci.add(player, ActionOnEntityUsePower.class, p -> p.shouldExecute(entity, hand, stackInHand, PriorityPhase.BEFORE));
        aipci.add(entity, ActionOnBeingUsedPower.class, p -> p.shouldExecute(player, hand, stackInHand, PriorityPhase.BEFORE));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

            if (!aipci.hasPowers(i)) {
                continue;
            }

            List<ActiveInteractionPower> aips = aipci.getPowers(i);
            ActionResult previousResult = ActionResult.PASS;

            for (ActiveInteractionPower aip : aips) {

                ActionResult currentResult = ActionResult.PASS;
                if (aip instanceof ActionOnEntityUsePower aoeup) {
                    currentResult = aoeup.executeAction(entity, hand);
                }

                else if (aip instanceof ActionOnBeingUsedPower aobup) {
                    currentResult = aobup.executeAction(player, hand);
                }

                if (ActionResultUtil.shouldOverride(previousResult, currentResult)) {
                    previousResult = currentResult;
                }

            }

            if (i == 0) {
                sharedZeroPriority$onEntity.set(previousResult);
                continue;
            }

            if (previousResult == ActionResult.PASS) {
                continue;
            }

            if (previousResult.shouldSwingHand()) {
                this.swingHand(hand);
            }

            return previousResult;

        }

        return original.call(entity, player, hand);

    }

    @ModifyReturnValue(method = "interact", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;", ordinal = 0)))
    private ActionResult apoli$afterEntityUse(ActionResult original, Entity entity, Hand hand, @Share("zeroPriority$onEntity") LocalRef<ActionResult> sharedZeroPriority$onEntity) {

        ActionResult cachedPriorityZeroResult = sharedZeroPriority$onEntity.get();
        ActionResult newResult = ActionResult.PASS;

        if (cachedPriorityZeroResult != null && cachedPriorityZeroResult != ActionResult.PASS) {
            newResult = cachedPriorityZeroResult;
        }

        else if (original == ActionResult.PASS) {

            ItemStack stackInHand = this.getStackInHand(hand);
            Prioritized.CallInstance<ActiveInteractionPower> aipci = new Prioritized.CallInstance<>();

            aipci.add(this, ActionOnEntityUsePower.class, p -> p.shouldExecute(entity, hand, stackInHand, PriorityPhase.AFTER));
            aipci.add(entity, ActionOnBeingUsedPower.class, p -> p.shouldExecute((PlayerEntity) (Object) this, hand, stackInHand, PriorityPhase.AFTER));

            for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {

                if (!aipci.hasPowers(i)) {
                    continue;
                }

                List<ActiveInteractionPower> aips = aipci.getPowers(i);
                ActionResult previousResult = ActionResult.PASS;

                for (ActiveInteractionPower aip : aips) {

                    ActionResult currentResult = ActionResult.PASS;
                    if (aip instanceof ActionOnEntityUsePower aoeup) {
                        currentResult = aoeup.executeAction(entity, hand);
                    }

                    else if (aip instanceof ActionOnBeingUsedPower aobup) {
                        currentResult = aobup.executeAction((PlayerEntity) (Object) this, hand);
                    }

                    if (ActionResultUtil.shouldOverride(previousResult, currentResult)) {
                        previousResult = currentResult;
                    }

                }

                if (previousResult != ActionResult.PASS) {
                    newResult = previousResult;
                    break;
                }

            }

        }

        if (newResult.shouldSwingHand()) {
            this.swingHand(hand);
        }

        return ActionResultUtil.shouldOverride(original, newResult)
            ? newResult
            : original;

    }

    @ModifyExpressionValue(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSprinting()Z"))
    private boolean apoli$shouldApplySprintJumpExhaustion(boolean original) {
        return original && this.apoli$applySprintJumpEffects();
    }

}
