package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.access.*;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDamageTypes;
import io.github.apace100.apoli.networking.packet.s2c.SyncAttackerS2CPacket;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ModifiableFoodEntity, MovingEntity, JumpingEntity {
    @Shadow
    protected abstract float getJumpVelocity();

    @Shadow
    public abstract float getMovementSpeed();

    @Shadow
    private Optional<BlockPos> climbingPos;

    @Shadow
    public abstract boolean isHoldingOntoLadder();

    @Shadow
    public abstract void setHealth(float health);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onStatusEffectApplied", at = @At("TAIL"))
    private void apoli$updateStatusEffectWhenApplied(StatusEffectInstance effectInstance, Entity source, CallbackInfo ci) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity) (Object) this, SyncStatusEffectsUtil.UpdateType.APPLY, effectInstance);
    }

    @Inject(method = "onStatusEffectUpgraded", at = @At("TAIL"))
    private void apoli$updateStatusEffectWhenUpgraded(StatusEffectInstance effectInstance, boolean reapplyEffect, Entity source, CallbackInfo ci) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity) (Object) this, SyncStatusEffectsUtil.UpdateType.UPGRADE, effectInstance);
    }

    @Inject(method = "onStatusEffectRemoved", at = @At("TAIL"))
    private void apoli$updateStatusEffectWhenRemoved(StatusEffectInstance effectInstance, CallbackInfo ci) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity) (Object) this, SyncStatusEffectsUtil.UpdateType.REMOVE, effectInstance);
    }

    @Inject(method = "clearStatusEffects", at = @At("RETURN"))
    private void apoli$updateStatusEffectWhenCleared(CallbackInfoReturnable<Boolean> cir) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity) (Object) this, SyncStatusEffectsUtil.UpdateType.CLEAR, null);
    }

    @ModifyVariable(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"), argsOnly = true)
    private StatusEffectInstance apoli$modifyStatusEffect(StatusEffectInstance original) {

        RegistryEntry<StatusEffect> effectType = original.getEffectType();

        float amplifier = PowerHolderComponent.modify(this, ModifyStatusEffectAmplifierPower.class, original.getAmplifier(), p -> p.doesApply(effectType));
        float duration = PowerHolderComponent.modify(this, ModifyStatusEffectDurationPower.class, original.getDuration(), p -> p.doesApply(effectType));

        return new StatusEffectInstance(
            effectType,
            Math.round(duration),
            Math.round(amplifier),
            original.isAmbient(),
            original.shouldShowParticles(),
            original.shouldShowIcon(),
            ((HiddenEffectStatus) original).apoli$getHiddenEffect()
        );

    }

    @Inject(method = "setAttacker", at = @At("TAIL"))
    private void apoli$syncAttacker(LivingEntity attacker, CallbackInfo ci) {

        if (this.getWorld().isClient) {
            return;
        }

        Optional<Integer> attackerId = Optional.ofNullable(this.attacker).map(Entity::getId);
        SyncAttackerS2CPacket syncAttackerPacket = new SyncAttackerS2CPacket(this.getId(), attackerId);

        for (ServerPlayerEntity player : PlayerLookup.tracking(this)) {
            ServerPlayNetworking.send(player, syncAttackerPacket);
        }

    }

    @ModifyReturnValue(method = "canWalkOnFluid", at = @At("RETURN"))
    private boolean apoli$letEntitiesWalkOnFluid(boolean original, FluidState fluidState) {
        return original
            || PowerHolderComponent.hasPower(this, WalkOnFluidPower.class, p -> fluidState.isIn(p.getFluidTag()));
    }

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float modifyHealingApplied(float originalValue) {
        return PowerHolderComponent.modify(this, ModifyHealingPower.class, originalValue);
    }

    @Unique
    private boolean apoli$hasModifiedDamage;

    @Unique
    private Optional<Boolean> apoli$shouldApplyArmor = Optional.empty();

    @Unique
    private Optional<Boolean> apoli$shouldDamageArmor = Optional.empty();

    @ModifyExpressionValue(method = "onDamaged", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSources;generic()Lnet/minecraft/entity/damage/DamageSource;"))
    private DamageSource apoli$overrideDamageSourceOnSync(DamageSource original, DamageSource source) {
        return this.getDamageSources().create(ApoliDamageTypes.SYNC_DAMAGE_SOURCE);
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float apoli$modifyDamageTaken(float original, DamageSource source, float amount) {

        if (source.isOf(ApoliDamageTypes.SYNC_DAMAGE_SOURCE)) {
            return original;
        }

        LivingEntity thisAsLiving = (LivingEntity) (Object) this;
        float newValue = original;

        if (source.getAttacker() != null && source.isIn(DamageTypeTags.IS_PROJECTILE)) {
            newValue = PowerHolderComponent.modify(source.getAttacker(), ModifyProjectileDamagePower.class, original,
                p -> p.doesApply(source, original, thisAsLiving),
                p -> p.executeActions(thisAsLiving));
        } else if (source.getAttacker() != null) {
            newValue = PowerHolderComponent.modify(source.getAttacker(), ModifyDamageDealtPower.class, original,
                p -> p.doesApply(source, original, thisAsLiving),
                p -> p.executeActions(thisAsLiving));
        }

        float intermediateValue = newValue;
        newValue = PowerHolderComponent.modify(this, ModifyDamageTakenPower.class, intermediateValue,
            p -> p.doesApply(source, intermediateValue),
            p -> p.executeActions(source.getAttacker()));

        apoli$hasModifiedDamage = newValue != original;
        List<ModifyDamageTakenPower> modifyDamageTakenPowers = PowerHolderComponent.getPowers(this, ModifyDamageTakenPower.class)
            .stream()
            .filter(mdtp -> mdtp.doesApply(source, original))
            .toList();

        long wantArmor = modifyDamageTakenPowers
            .stream()
            .filter(mdtp -> mdtp.modifiesArmorApplicance() && mdtp.shouldApplyArmor())
            .count();
        long dontWantArmor = modifyDamageTakenPowers
            .stream()
            .filter(mdtp -> mdtp.modifiesArmorApplicance() && !mdtp.shouldApplyArmor())
            .count();
        apoli$shouldApplyArmor = wantArmor == dontWantArmor ? Optional.empty() : Optional.of(wantArmor > dontWantArmor);

        long wantDamage = modifyDamageTakenPowers
            .stream()
            .filter(mdtp -> mdtp.modifiesArmorDamaging() && mdtp.shouldDamageArmor())
            .count();
        long dontWantDamage = modifyDamageTakenPowers
            .stream()
            .filter(mdtp -> mdtp.modifiesArmorDamaging() && !mdtp.shouldDamageArmor())
            .count();
        apoli$shouldDamageArmor = wantDamage == dontWantDamage ? Optional.empty() : Optional.of(wantDamage > dontWantDamage);

        return newValue;

    }

    @ModifyExpressionValue(method = "applyArmorToDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSource;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean apoli$allowApplyingOrDamagingArmor(boolean original, DamageSource source, float amount) {

        if (apoli$shouldApplyArmor.isEmpty() && (original && apoli$shouldDamageArmor.orElse(false))) {
            this.damageArmor(source, amount);
        }

        return apoli$shouldApplyArmor
            .map(result -> !result)
            .orElse(original);

    }

    @WrapWithCondition(method = "applyArmorToDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageArmor(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private boolean apoli$allowDamagingArmor(LivingEntity instance, DamageSource source, float amount) {
        return apoli$shouldDamageArmor.orElse(true);
    }

    @ModifyExpressionValue(method = "applyArmorToDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/DamageUtil;getDamageLeft(Lnet/minecraft/entity/LivingEntity;FLnet/minecraft/entity/damage/DamageSource;FF)F"))
    private float apoli$allowApplyingArmor(float modified, DamageSource source, float original) {
        return apoli$shouldApplyArmor.orElse(true)
            ? modified
            : original;
    }

    @ModifyExpressionValue(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isDead()Z", ordinal = 0))
    private boolean apoli$preventHitIfDamageIsZero(boolean original, DamageSource source, float amount) {
        return original || apoli$hasModifiedDamage && amount <= 0.0F;
    }

    @Inject(method = "damage", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSleeping()Z")))
    private void apoli$invokeHitActions(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        if (!cir.getReturnValue()) {
            return;
        }

        Entity attacker = source.getAttacker();

        PowerHolderComponent.withPowers(this, ActionWhenHitPower.class, p -> p.doesApply(attacker, source, amount), p -> p.whenHit(attacker));
        PowerHolderComponent.withPowers(attacker, ActionOnHitPower.class, p -> p.doesApply(this, source, amount), p -> p.onHit(this));

        PowerHolderComponent.withPowers(this, SelfActionWhenHitPower.class, p -> p.doesApply(source, amount), SelfActionWhenHitPower::whenHit);
        PowerHolderComponent.withPowers(this, AttackerActionWhenHitPower.class, p -> p.doesApply(source, amount), p -> p.whenHit(attacker));

        PowerHolderComponent.withPowers(attacker, SelfActionOnHitPower.class, p -> p.doesApply(this, source, amount), SelfActionOnHitPower::onHit);
        PowerHolderComponent.withPowers(attacker, TargetActionOnHitPower.class, p -> p.doesApply(this, source, amount), p -> p.onHit(this));

    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;onDeath(Lnet/minecraft/entity/damage/DamageSource;)V"))
    private void invokeDeathAction(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PowerHolderComponent.withPowers(this, ActionOnDeathPower.class, p -> p.doesApply(source.getAttacker(), source, amount), p -> p.onDeath(source.getAttacker()));
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;onDeath(Lnet/minecraft/entity/damage/DamageSource;)V"))
    private void invokeKillAction(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PowerHolderComponent.getPowers(source.getAttacker(), SelfActionOnKillPower.class).forEach(p -> p.onKill((LivingEntity)(Object)this, source, amount));
    }

    @ModifyExpressionValue(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isWet()Z"))
    private boolean apoli$preventExtinguishingWhenPowerSwimming(boolean original) {

        if (this.isSwimming() && this.getFluidHeight(FluidTags.WATER) <= 0 && PowerHolderComponent.hasPower(this, SwimmingPower.class)) {
            return false;
        }

        else {
            return original;
        }

    }

    @Unique
    private boolean prevPowderSnowState = false;

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getFrozenTicks()I"))
    private void freezeEntityFromPower(CallbackInfo ci) {
        if(PowerHolderComponent.hasPower(this, FreezePower.class)) {
            this.prevPowderSnowState = this.inPowderSnow;
            this.inPowderSnow = true;
        }
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;removePowderSnowSlow()V"))
    private void unfreezeEntityFromPower(CallbackInfo ci) {
        if(PowerHolderComponent.hasPower(this, FreezePower.class)) {
            this.inPowderSnow = this.prevPowderSnowState;
        }
    }

    @Inject(method = "canFreeze", at = @At("RETURN"), cancellable = true)
    private void allowFreezingPower(CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.hasPower(this, FreezePower.class)) {
            cir.setReturnValue(true);
        }
    }

    //  TODO: This is deprecated. Remove this! -eggohito
    // SetEntityGroupPower
//    @ModifyReturnValue(method = "getGroup", at = @At("RETURN"))
//    private EntityGroup apoli$replaceGroup(EntityGroup original) {
//        return PowerHolderComponent.getPowers(this, SetEntityGroupPower.class)
//            .stream()
//            .max(Comparator.comparing(SetEntityGroupPower::getPriority))
//            .map(SetEntityGroupPower::getGroup)
//            .orElse(original);
//    }

    @Unique
    private boolean apoli$applySprintJumpingEffects;

    @Override
    public boolean apoli$applySprintJumpEffects() {
        return apoli$applySprintJumpingEffects;
    }

    // SPRINT_JUMP
    @ModifyReturnValue(method = "getJumpVelocity", at = @At("RETURN"))
    private float apoli$modifyJumpVelocity(float original) {

        float modified = PowerHolderComponent.modify(this, ModifyJumpPower.class, original, p -> true, ModifyJumpPower::executeAction);
        this.apoli$applySprintJumpingEffects = modified > 0;

        return modified;

    }

    @ModifyExpressionValue(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z"))
    private boolean apoli$shouldApplySprintJumpEffects(boolean original) {
        return original && this.apoli$applySprintJumpEffects();
    }

    // HOTBLOODED
    @Inject(at = @At("HEAD"), method= "canHaveStatusEffect", cancellable = true)
    private void preventStatusEffects(StatusEffectInstance effect, CallbackInfoReturnable<Boolean> info) {
        for (EffectImmunityPower power : PowerHolderComponent.getPowers(this, EffectImmunityPower.class)) {
            if(power.doesApply(effect)) {
                info.setReturnValue(false);
                return;
            }
        }
    }

    // CLIMBING
    @ModifyReturnValue(method = "isClimbing", at = @At("RETURN"))
    private boolean apoli$modifyClimbing(boolean original) {

        if (original) {
            return true;
        }

        List<ClimbingPower> climbingPowers = PowerHolderComponent.getPowers(this, ClimbingPower.class);
        if (this.isSpectator() || climbingPowers.isEmpty()) {
            return false;
        }

        this.climbingPos = Optional.of(this.getBlockPos());
        return true;

    }

    @ModifyReturnValue(method = "isHoldingOntoLadder", at = @At("RETURN"))
    private boolean apoli$overrideClimbHold(boolean original) {

        List<ClimbingPower> climbingPowers = PowerHolderComponent.getPowers(this, ClimbingPower.class);
        if (climbingPowers.isEmpty()) {
            return original;
        }

        return climbingPowers
            .stream()
            .anyMatch(ClimbingPower::canHold);

    }

    // SLOW_FALLING
    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"), method = "travel", name = "d", ordinal = 0)
    public double modifyFallingVelocity(double in) {
        if(this.getVelocity().y > 0D) {
            return in;
        }
        List<ModifyFallingPower> modifyFallingPowers = PowerHolderComponent.getPowers(this, ModifyFallingPower.class);
        if(!modifyFallingPowers.isEmpty()) {

            if(modifyFallingPowers.stream().anyMatch(p -> !p.takeFallDamage)) {
                this.fallDistance = 0;
            }
            return PowerHolderComponent.modify(this, ModifyFallingPower.class, in);
        }
        return in;
    }

    @Inject(method = "getAttributes", at = @At("RETURN"))
    private void apoli$setAttributeContainerOwner(CallbackInfoReturnable<AttributeContainer> cir) {

        if (cir.getReturnValue() instanceof OwnableAttributeContainer ownableAttributeContainer) {
            ownableAttributeContainer.apoli$setOwner(this);
        }

    }

    @ModifyVariable(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isOnGround()Z", opcode = Opcodes.GETFIELD, ordinal = 2))
    private float modifySlipperiness(float original) {
        return PowerHolderComponent.modify(this, ModifySlipperinessPower.class, original, p -> p.doesApply(getWorld(), getVelocityAffectingPos()));
    }

    @ModifyExpressionValue(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isDead()Z", ordinal = 1))
    private boolean apoli$preventDeath(boolean original, DamageSource source, float amount) {

        if (original && PreventDeathPower.doesPrevent(this, source, amount)) {
            this.setHealth(1.0F);
            return false;
        }

        return original;

    }

    @ModifyVariable(method = "eatFood", at = @At("HEAD"), argsOnly = true)
    private ItemStack apoli$modifyEatenStack(ItemStack original) {

        LivingEntity thisAsLiving = (LivingEntity) (Object) this;
        if (thisAsLiving instanceof PlayerEntity) {
            return original;
        }

        StackReference newStackRef = InventoryUtil.createStackReference(original);
        List<ModifyFoodPower> modifyFoodPowers = PowerHolderComponent.getPowers(this, ModifyFoodPower.class)
            .stream()
            .filter(mfp -> mfp.doesApply(original))
            .toList();

        for (ModifyFoodPower modifyFoodPower : modifyFoodPowers) {
            modifyFoodPower.setConsumedItemStackReference(newStackRef);
        }

        EdibleItemPower.get(original.copy(), this).ifPresent(this::apoli$setEdibleItemPower);

        this.apoli$setCurrentModifyFoodPowers(modifyFoodPowers);
        this.apoli$setOriginalFoodStack(original);

        return newStackRef.get();

    }

    @ModifyVariable(method = "eatFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyFoodEffects(Lnet/minecraft/component/type/FoodComponent;)V", shift = At.Shift.AFTER), argsOnly = true)
    private ItemStack apoli$restoreOriginalEatenStack(ItemStack modified) {
        ItemStack original = this.apoli$getOriginalFoodStack();
        return original == null
            ? modified
            : original;
    }

    @ModifyReturnValue(method = "eatFood", at = @At("RETURN"))
    private ItemStack apoli$modifyCustomFoodAndCleanUp(ItemStack original) {

        EdibleItemPower edibleItemPower = this.apoli$getEdibleItemPower();
        ItemStack result = original;

        modifyCustomFood:
        if (edibleItemPower != null) {

            edibleItemPower.executeEntityAction();

            StackReference newStackRef = InventoryUtil.createStackReference(original);
            StackReference resultStackRef = edibleItemPower.executeItemActions(newStackRef);

            ItemStack newStack = newStackRef.get();
            ItemStack resultStack = resultStackRef.get();

            if (resultStackRef == StackReference.EMPTY) {
                result = newStack;
                break modifyCustomFood;
            }

            else if (newStack.isEmpty()) {
                result = resultStack;
                break modifyCustomFood;
            }

            else if (ItemStack.areEqual(resultStack, newStack)) {
                newStack.increment(1);
            }

            else if ((LivingEntity) (Object) this instanceof PlayerEntity player && !player.isCreative()) {
                player.getInventory().offerOrDrop(resultStack);
            }

            else {
                InventoryUtil.throwItem(this, resultStack, false, false);
            }

            result = newStack;

        }

        this.apoli$setCurrentModifyFoodPowers(new LinkedList<>());
        this.apoli$setOriginalFoodStack(null);
        this.apoli$setEdibleItemPower(null);

        return result;

    }

    @Inject(method = "applyFoodEffects", at = @At("HEAD"), cancellable = true)
    private void apoli$preventApplyingFoodEffects(FoodComponent component, CallbackInfo ci) {
        if (this.apoli$getCurrentModifyFoodPowers().stream().anyMatch(ModifyFoodPower::doesPreventEffects)) {
            ci.cancel();
        }
    }

    @Shadow @Nullable private LivingEntity attacker;

    @Shadow public abstract void damageArmor(DamageSource source, float amount);

    @Shadow public abstract int getArmor();

    @Shadow public abstract AttributeContainer getAttributes();

    @Shadow public abstract boolean isClimbing();

    @Shadow public abstract boolean isDead();

    @Shadow public abstract double getAttributeValue(RegistryEntry<EntityAttribute> attribute);

    @Shadow public abstract float getArmorVisibility();

    @Shadow public abstract boolean damage(DamageSource source, float amount);

    @Shadow protected abstract void onStatusEffectRemoved(StatusEffectInstance effect);

    @Shadow public float sidewaysSpeed;

    @Shadow public float forwardSpeed;

    @Shadow public abstract double getAttributeBaseValue(RegistryEntry<EntityAttribute> attribute);

    @Inject(method = "getOffGroundSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyFlySpeed(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(PowerHolderComponent.modify(this, ModifyAirSpeedPower.class, cir.getReturnValue()));
    }

    @WrapOperation(method = "getAttackDistanceScalingFactor", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvisible()Z"))
    private boolean apoli$specificallyInvisibleTo(LivingEntity livingEntity, Operation<Boolean> original, @Nullable Entity viewer) {

        List<InvisibilityPower> invisibilityPowers = PowerHolderComponent.getPowers(livingEntity, InvisibilityPower.class, true);
        if (viewer == null || invisibilityPowers.isEmpty()) {
            return original.call(livingEntity);
        }

        return invisibilityPowers
            .stream()
            .anyMatch(p -> p.isActive() && p.doesApply(viewer));

    }

    @ModifyExpressionValue(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSprinting()Z"))
    private boolean apoli$cancelOutJumpVelocityIfNotMovingWithSprintPower(boolean original) {
        // The movement check is here so this doesn't happen if the player is moving at a sprinting amount.
        if (PowerHolderComponent.hasPower(this, SprintingPower.class) && this.apoli$getHorizontalMovementValue() < this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)) {
            return false;
        }
        return original;
    }

    @Unique
    private List<ModifyFoodPower> apoli$currentModifyFoodPowers = new LinkedList<>();

    @Unique
    private ItemStack apoli$originalFoodStack;

    @Unique
    private EdibleItemPower apoli$edibleItemPower;

    @Override
    public List<ModifyFoodPower> apoli$getCurrentModifyFoodPowers() {
        return apoli$currentModifyFoodPowers;
    }

    @Override
    public void apoli$setCurrentModifyFoodPowers(List<ModifyFoodPower> powers) {
        apoli$currentModifyFoodPowers = powers;
    }

    @Override
    public ItemStack apoli$getOriginalFoodStack() {
        return apoli$originalFoodStack;
    }

    @Override
    public void apoli$setOriginalFoodStack(ItemStack original) {
        apoli$originalFoodStack = original;
    }

    @Override
    public EdibleItemPower apoli$getEdibleItemPower() {
        return apoli$edibleItemPower;
    }

    @Override
    public void apoli$setEdibleItemPower(EdibleItemPower power) {
        this.apoli$edibleItemPower = power;
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void updateItemStackHolder(CallbackInfo ci) {
        InventoryUtil.forEachStack(this, stack -> ((EntityLinkedItemStack) stack).apoli$setEntity(this));
    }

}
