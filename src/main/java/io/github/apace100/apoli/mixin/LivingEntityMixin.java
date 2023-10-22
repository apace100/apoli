package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.*;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDamageTypes;
import io.github.apace100.apoli.networking.packet.s2c.SyncAttackerS2CPacket;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.StackPowerUtil;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ModifiableFoodEntity, MovingEntity {
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

        StatusEffect effectType = original.getEffectType();

        float amplifier = PowerHolderComponent.modify(this, ModifyStatusEffectAmplifierPower.class, original.getAmplifier(), p -> p.doesApply(effectType));
        float duration = PowerHolderComponent.modify(this, ModifyStatusEffectDurationPower.class, original.getDuration(), p -> p.doesApply(effectType));

        return new StatusEffectInstance(
            effectType,
            Math.round(duration),
            Math.round(amplifier),
            original.isAmbient(),
            original.shouldShowParticles(),
            original.shouldShowIcon(),
            ((HiddenEffectStatus) original).apoli$getHiddenEffect(),
            original.getFactorCalculationData()
        );

    }

    @Inject(method = "setAttacker", at = @At("TAIL"))
    private void apoli$syncAttacker(LivingEntity attacker, CallbackInfo ci) {

        if (this.getWorld().isClient) {
            return;
        }

        OptionalInt attackerId = this.attacker != null ? OptionalInt.of(this.attacker.getId()) : OptionalInt.empty();
        SyncAttackerS2CPacket syncAttackerPacket = new SyncAttackerS2CPacket(this.getId(), attackerId);

        for (ServerPlayerEntity player : PlayerLookup.tracking(this)) {
            ServerPlayNetworking.send(player, syncAttackerPacket);
        }

    }

    @Inject(method = "getEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/AttributeContainer;removeModifiers(Lcom/google/common/collect/Multimap;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void removeEquipmentPowers(CallbackInfoReturnable<Map> cir, Map map, EquipmentSlot var2[], int var3, int var4, EquipmentSlot equipmentSlot, ItemStack itemStack3, ItemStack itemStack4) {
        List<StackPowerUtil.StackPower> powers = StackPowerUtil.getPowers(itemStack3, equipmentSlot);
        if(powers.size() > 0) {
            Identifier source = new Identifier(Apoli.MODID, equipmentSlot.getName());
            PowerHolderComponent powerHolder = PowerHolderComponent.KEY.get(this);
            powers.forEach(sp -> {
                if(PowerTypeRegistry.contains(sp.powerId)) {
                    powerHolder.removePower(PowerTypeRegistry.get(sp.powerId), source);
                }
            });
            powerHolder.sync();
        }
    }

    @Inject(method = "getEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/AttributeContainer;addTemporaryModifiers(Lcom/google/common/collect/Multimap;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addEquipmentPowers(CallbackInfoReturnable<Map> cir, Map map, EquipmentSlot var2[], int var3, int var4, EquipmentSlot equipmentSlot, ItemStack itemStack3, ItemStack itemStack4) {
        List<StackPowerUtil.StackPower> powers = StackPowerUtil.getPowers(itemStack4, equipmentSlot);
        if(powers.size() > 0) {
            Identifier source = new Identifier(Apoli.MODID, equipmentSlot.getName());
            PowerHolderComponent powerHolder = PowerHolderComponent.KEY.get(this);
            powers.forEach(sp -> {
                if(PowerTypeRegistry.contains(sp.powerId)) {
                    powerHolder.addPower(PowerTypeRegistry.get(sp.powerId), source);
                }
            });
            powerHolder.sync();
        } else if(StackPowerUtil.getPowers(itemStack3, equipmentSlot).size() > 0) {
            PowerHolderComponent.KEY.get(this).sync();
        }

    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    private void modifyWalkableFluids(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        if(PowerHolderComponent.getPowers(this, WalkOnFluidPower.class).stream().anyMatch(p -> fluidState.isIn(p.getFluidTag()))) {
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float modifyHealingApplied(float originalValue) {
        return PowerHolderComponent.modify(this, ModifyHealingPower.class, originalValue);
    }

    @Unique
    private boolean apoli$hasModifiedDamage;

    @Unique
    private Optional<Boolean> apoli$shouldApplyArmor;

    @Unique
    private Optional<Boolean> apoli$shouldDamageArmor;

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

    @ModifyExpressionValue(method = "applyArmorToDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/DamageUtil;getDamageLeft(FFF)F"))
    private float apoli$allowApplyingArmor(float modified, DamageSource source, float original) {
        return apoli$shouldApplyArmor.orElse(true) ? modified : original;
    }

    @ModifyExpressionValue(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isDead()Z", ordinal = 0))
    private boolean apoli$preventHitIfDamageIsZero(boolean original, DamageSource source, float amount) {
        return original || apoli$hasModifiedDamage && amount <= 0F;
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void invokeHitActions(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue()) {
            Entity attacker = source.getAttacker();
            if(attacker != null) {
                PowerHolderComponent.withPowers(this, ActionWhenHitPower.class, p -> true, p -> p.whenHit(attacker, source, amount));
                PowerHolderComponent.withPowers(attacker, ActionOnHitPower.class, p -> true, p -> p.onHit(this, source, amount));
            }
            PowerHolderComponent.getPowers(this, SelfActionWhenHitPower.class).forEach(p -> p.whenHit(source, amount));
            PowerHolderComponent.getPowers(this, AttackerActionWhenHitPower.class).forEach(p -> p.whenHit(source, amount));
            PowerHolderComponent.getPowers(source.getAttacker(), SelfActionOnHitPower.class).forEach(p -> p.onHit((LivingEntity)(Object)this, source, amount));
            PowerHolderComponent.getPowers(source.getAttacker(), TargetActionOnHitPower.class).forEach(p -> p.onHit((LivingEntity)(Object)this, source, amount));

        }
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;onDeath(Lnet/minecraft/entity/damage/DamageSource;)V"))
    private void invokeDeathAction(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PowerHolderComponent.withPowers(this, ActionOnDeathPower.class, p -> p.doesApply(source.getAttacker(), source, amount), p -> p.onDeath(source.getAttacker()));
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;onDeath(Lnet/minecraft/entity/damage/DamageSource;)V"))
    private void invokeKillAction(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PowerHolderComponent.getPowers(source.getAttacker(), SelfActionOnKillPower.class).forEach(p -> p.onKill((LivingEntity)(Object)this, source, amount));
    }

    @Redirect(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isWet()Z"))
    private boolean preventExtinguishingFromSwimming(LivingEntity livingEntity) {
        if(PowerHolderComponent.hasPower(livingEntity, SwimmingPower.class) && livingEntity.isSwimming() && !(getFluidHeight(FluidTags.WATER) > 0)) {
            return false;
        }
        return livingEntity.isWet();
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

    // SetEntityGroupPower
    @ModifyReturnValue(method = "getGroup", at = @At("RETURN"))
    private EntityGroup apoli$replaceGroup(EntityGroup original) {
        return PowerHolderComponent.getPowers(this, SetEntityGroupPower.class)
            .stream()
            .max(Comparator.comparing(SetEntityGroupPower::getPriority))
            .map(SetEntityGroupPower::getGroup)
            .orElse(original);
    }

    // SPRINT_JUMP
    @Redirect(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getJumpVelocity()F"))
    private float modifyJumpVelocity(LivingEntity entity) {
        return PowerHolderComponent.modify(this, ModifyJumpPower.class, this.getJumpVelocity(), p -> {
            p.executeAction();
            return true;
        });
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


    @Unique
    private boolean apoli$activelyClimbing = false;

    @Override
    public boolean apoli$activelyClimbing() {
        return apoli$activelyClimbing;
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void apoli$getPrevY(CallbackInfo ci) {
        this.apoli$activelyClimbing = false;
    }

    // CLIMBING
    @ModifyReturnValue(method = "isClimbing", at = @At("RETURN"))
    private boolean apoli$modifyClimbing(boolean original) {

        if (original) {

            if (this.getY() != this.prevY) {
                this.apoli$activelyClimbing = true;
            }

            return true;

        }

        List<ClimbingPower> climbingPowers = PowerHolderComponent.getPowers(this, ClimbingPower.class);
        if (this.isSpectator() || climbingPowers.isEmpty()) {
            return false;
        }

        this.climbingPos = Optional.of(this.getBlockPos());
        if (this.getY() != this.prevY) {
            this.apoli$activelyClimbing = true;
        }

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
        if(modifyFallingPowers.size() > 0) {

            if(modifyFallingPowers.stream().anyMatch(p -> !p.takeFallDamage)) {
                this.fallDistance = 0;
            }
            return PowerHolderComponent.modify(this, ModifyFallingPower.class, in);
        }
        return in;
    }

    @ModifyReturnValue(method = "getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D", at = @At("RETURN"))
    private double apoli$modifyAttributeValue(double original, EntityAttribute attribute) {
        return PowerHolderComponent.modify(this, ModifyAttributePower.class, (float) original, p -> p.getAttribute() == attribute);
    }

    @Inject(method = "getAttributeInstance", at = @At("RETURN"))
    private void apoli$setEntityToAttributeInstance(EntityAttribute attribute, CallbackInfoReturnable<EntityAttributeInstance> cir) {
        EntityAttributeInstance instance = cir.getReturnValue();
        if (instance != null) {
            ((EntityAttributeInstanceAccess) instance).apoli$setEntity(this);
        }
    }


    @ModifyVariable(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isOnGround()Z", opcode = Opcodes.GETFIELD, ordinal = 2))
    private float modifySlipperiness(float original) {
        return PowerHolderComponent.modify(this, ModifySlipperinessPower.class, original, p -> p.doesApply(getWorld(), getVelocityAffectingPos()));
    }

    @Unique
    private float cachedDamageAmount;

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tryUseTotem(Lnet/minecraft/entity/damage/DamageSource;)Z"))
    private void cacheDamageAmount(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.cachedDamageAmount = amount;
    }

    @Inject(method = "tryUseTotem", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Hand;values()[Lnet/minecraft/util/Hand;"), cancellable = true)
    private void preventDeath(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        Optional<PreventDeathPower> preventDeathPower = PowerHolderComponent.getPowers(this, PreventDeathPower.class).stream().filter(p -> p.doesApply(source, cachedDamageAmount)).findFirst();
        if(preventDeathPower.isPresent()) {
            this.setHealth(1.0F);
            preventDeathPower.get().executeAction();
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "eatFood", at = @At("HEAD"), argsOnly = true)
    private ItemStack apoli$modifyEatenItemStack(ItemStack original) {

        LivingEntity thisAsLiving = (LivingEntity) (Object) this;
        if (thisAsLiving instanceof PlayerEntity) {
            return original;
        }

        ItemStack newStack = original;
        List<ModifyFoodPower> modifyFoodPowers = PowerHolderComponent.getPowers(this, ModifyFoodPower.class)
            .stream()
            .filter(mfp -> mfp.doesApply(original))
            .toList();

        for (ModifyFoodPower modifyFoodPower : modifyFoodPowers) {
            newStack = modifyFoodPower.getConsumedItemStack(newStack);
        }

        this.apoli$setCurrentModifyFoodPowers(modifyFoodPowers);
        this.apoli$setOriginalFoodStack(original);

        return newStack;

    }

    @ModifyVariable(method = "eatFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyFoodEffects(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)V", shift = At.Shift.AFTER), argsOnly = true)
    private ItemStack apoli$unmodifyEatenItemStack(ItemStack modified) {

        ItemStack original = this.apoli$getOriginalFoodStack();
        if (original == null) {
            return modified;
        }

        this.apoli$setOriginalFoodStack(null);
        return original;

    }

    @Inject(method = "eatFood", at = @At("TAIL"))
    private void apoli$removeCurrentModifyFoodPowers(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        this.apoli$setCurrentModifyFoodPowers(new LinkedList<>());
    }

    @Inject(method = "applyFoodEffects", at = @At("HEAD"), cancellable = true)
    private void apoli$preventApplyingFoodEffects(ItemStack stack, World world, LivingEntity targetEntity, CallbackInfo ci) {
        if (this.apoli$getCurrentModifyFoodPowers().stream().anyMatch(ModifyFoodPower::doesPreventEffects)) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "eatFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isFood()Z"))
    private boolean apoli$allowConsumingCustomFood(boolean original, World world, ItemStack stack) {
        return original || ((PotentiallyEdibleItemStack) stack).apoli$getFoodComponent().isPresent();
    }

    @Shadow @Nullable private LivingEntity attacker;

    @Shadow protected abstract void applyFoodEffects(ItemStack stack, World world, LivingEntity targetEntity);

    @Shadow public abstract void damageArmor(DamageSource source, float amount);

    @Shadow public abstract int getArmor();

    @Shadow public abstract double getAttributeValue(EntityAttribute attribute);

    @Shadow public abstract AttributeContainer getAttributes();

    @Shadow public abstract boolean isClimbing();

    @Shadow public abstract boolean isDead();

    @Shadow public abstract double getAttributeValue(RegistryEntry<EntityAttribute> attribute);

    @Shadow public abstract double getAttributeBaseValue(EntityAttribute attribute);

    @Shadow public abstract float getArmorVisibility();

    @Shadow public abstract boolean hasStatusEffect(StatusEffect effect);

    @Shadow public abstract boolean damage(DamageSource source, float amount);

    @Inject(method = "getOffGroundSpeed", at = @At("RETURN"), cancellable = true)
    private void modifyFlySpeed(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(PowerHolderComponent.modify(this, ModifyAirSpeedPower.class, cir.getReturnValue()));
    }

    @Redirect(method = "getAttackDistanceScalingFactor", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvisible()Z"))
    private boolean invisibilityException(LivingEntity instance, Entity entity) {
        if (entity == null || !PowerHolderComponent.hasPower(this, InvisibilityPower.class)) {
            return instance.isInvisible();
        } else {
            return PowerHolderComponent.hasPower(this, InvisibilityPower.class, p -> p.doesApply(entity));
        }
    }

    @Unique
    private List<ModifyFoodPower> apoli$currentModifyFoodPowers = new LinkedList<>();

    @Unique
    private ItemStack apoli$originalFoodStack;

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

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void updateItemStackHolder(CallbackInfo ci) {
        InventoryUtil.forEachStack(this, stack -> ((EntityLinkedItemStack) stack).apoli$setEntity(this), stack -> ((EntityLinkedItemStack) stack).apoli$setEntity(this));
    }
}
