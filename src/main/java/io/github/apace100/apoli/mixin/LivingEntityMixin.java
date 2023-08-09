package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.access.HiddenEffectStatus;
import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.ModPackets;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.StackPowerUtil;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.tag.FluidTags;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ModifiableFoodEntity {
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
    private void updateStatusEffectWhenApplied(StatusEffectInstance effectInstance, Entity source, CallbackInfo ci) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity)(Object)this, SyncStatusEffectsUtil.UpdateType.APPLY, effectInstance);
    }

    @Inject(method = "onStatusEffectUpgraded", at = @At("TAIL"))
    private void updateStatusEffectWhenUpgraded(StatusEffectInstance effectInstance, boolean reapplyEffect, Entity source, CallbackInfo ci) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity)(Object)this, SyncStatusEffectsUtil.UpdateType.UPGRADE, effectInstance);
    }

    @Inject(method = "onStatusEffectRemoved", at = @At("TAIL"))
    private void updateStatusEffectWhenRemoved(StatusEffectInstance effectInstance, CallbackInfo ci) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity)(Object)this, SyncStatusEffectsUtil.UpdateType.REMOVE, effectInstance);
    }

    @Inject(method = "clearStatusEffects", at = @At("RETURN"))
    private void updateStatusEffectWhenCleared(CallbackInfoReturnable<Boolean> cir) {
        SyncStatusEffectsUtil.sendStatusEffectUpdatePacket((LivingEntity)(Object)this, SyncStatusEffectsUtil.UpdateType.CLEAR, null);
    }

    @ModifyVariable(method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z", at = @At("HEAD"))
    private StatusEffectInstance modifyStatusEffect(StatusEffectInstance effect) {
        StatusEffect effectType = effect.getEffectType();
        int originalAmp = effect.getAmplifier();
        int originalDur = effect.getDuration();

        int amplifier = Math.round(PowerHolderComponent.modify(this, ModifyStatusEffectAmplifierPower.class, originalAmp, power -> power.doesApply(effectType)));
        int duration = Math.round(PowerHolderComponent.modify(this, ModifyStatusEffectDurationPower.class, originalDur, power -> power.doesApply(effectType)));

        if (amplifier != originalAmp || duration != originalDur) {
            return new StatusEffectInstance(
                    effectType,
                    duration,
                    amplifier,
                    effect.isAmbient(),
                    effect.shouldShowParticles(),
                    effect.shouldShowIcon(),
                    ((HiddenEffectStatus) effect).getHiddenEffect(),
                    Optional.empty()
            );
        }
        return effect;
    }

    @Inject(method = "setAttacker", at = @At("TAIL"))
    private void syncAttacker(LivingEntity attacker, CallbackInfo ci) {
        if(!getWorld().isClient) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(getId());
            if(this.attacker == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                buf.writeInt(this.attacker.getId());
            }
            for (ServerPlayerEntity player : PlayerLookup.tracking(this)) {
                ServerPlayNetworking.send(player, ModPackets.SET_ATTACKER, buf);
            }
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

    private boolean apoli$hasModifiedDamage;
    private Optional<Boolean> apoli$shouldApplyArmor;
    private Optional<Boolean> apoli$shouldDamageArmor;

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float modifyDamageTaken(float originalValue, DamageSource source, float amount) {
        float newValue = originalValue;
        LivingEntity thisAsLiving = (LivingEntity)(Object)this;
        if(source.getAttacker() != null) {
            if (!source.isIn(DamageTypeTags.IS_PROJECTILE)) {
                newValue = PowerHolderComponent.modify(source.getAttacker(), ModifyDamageDealtPower.class, originalValue,
                    p -> p.doesApply(source, originalValue, thisAsLiving), p -> p.executeActions(thisAsLiving));
            } else {
                newValue = PowerHolderComponent.modify(
                    source.getAttacker(), ModifyProjectileDamagePower.class, originalValue,
                    p -> p.doesApply(source, originalValue, thisAsLiving), p -> p.executeActions(thisAsLiving));
            }
        }

        float intermediateValue = newValue;
        newValue = PowerHolderComponent.modify(this, ModifyDamageTakenPower.class,
            intermediateValue, p -> p.doesApply(source, intermediateValue), p -> p.executeActions(source.getAttacker()));

        apoli$hasModifiedDamage = newValue != originalValue;

        List<ModifyDamageTakenPower> mdtps = PowerHolderComponent.getPowers(this, ModifyDamageTakenPower.class).stream().filter(p -> p.doesApply(source, originalValue)).toList();
        long wantArmor = mdtps.stream().filter(p -> p.modifiesArmorApplicance() && p.shouldApplyArmor()).count();
        long dontWantArmor = mdtps.stream().filter(p -> p.modifiesArmorApplicance() && !p.shouldApplyArmor()).count();
        apoli$shouldApplyArmor = wantArmor == dontWantArmor ? Optional.empty() : Optional.of(wantArmor > dontWantArmor);
        long wantDamage = mdtps.stream().filter(p -> p.modifiesArmorDamaging() && p.shouldDamageArmor()).count();
        long dontWantDamage = mdtps.stream().filter(p -> p.modifiesArmorDamaging() && !p.shouldDamageArmor()).count();
        apoli$shouldDamageArmor = wantDamage == dontWantDamage ? Optional.empty() : Optional.of(wantDamage > dontWantDamage);

        return newValue;
    }

    @Inject(method = "applyArmorToDamage", at = @At("HEAD"), cancellable = true)
    private void modifyArmorApplicance(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        if(apoli$shouldApplyArmor.isPresent()) {
            if(apoli$shouldDamageArmor.isPresent() && apoli$shouldDamageArmor.get()) {
                this.damageArmor(source, amount);
            }
            if(apoli$shouldApplyArmor.get()) {
                if(apoli$shouldDamageArmor.isEmpty()) {
                    this.damageArmor(source, amount);
                }
                float damageLeft = DamageUtil.getDamageLeft(amount, this.getArmor(), (float)this.getAttributeValue(EntityAttributes.GENERIC_ARMOR_TOUGHNESS));
                cir.setReturnValue(damageLeft);
            } else {
                cir.setReturnValue(amount);
            }
        } else {
            if(apoli$shouldDamageArmor.isPresent()) {
                if(apoli$shouldDamageArmor.get() && source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
                    this.damageArmor(source, amount);
                }
            }
        }
    }

    @Redirect(method = "applyArmorToDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageArmor(Lnet/minecraft/entity/damage/DamageSource;F)V"))
    private void preventArmorDamaging(LivingEntity instance, DamageSource source, float amount) {
        if(apoli$shouldDamageArmor.isPresent()) {
            if(!apoli$shouldDamageArmor.get()) {
                return;
            }
        }
        this.damageArmor(source, amount);
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSleeping()Z"), cancellable = true)
    private void preventHitIfDamageIsZero(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(apoli$hasModifiedDamage && amount <= 0f) {
            cir.setReturnValue(false);
        }
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
    @Inject(at = @At("HEAD"), method = "getGroup", cancellable = true)
    public void getGroup(CallbackInfoReturnable<EntityGroup> info) {
        if((Object)this instanceof LivingEntity) {
            PowerHolderComponent component = PowerHolderComponent.KEY.get(this);
            List<SetEntityGroupPower> groups = component.getPowers(SetEntityGroupPower.class);
            if(groups.size() > 0) {
                if(groups.size() > 1) {
                    Apoli.LOGGER.warn("Entity " + this.getDisplayName().toString() + " has two instances of SetEntityGroupPower.");
                }
                info.setReturnValue(groups.get(0).group);
            }
        }
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

    // CLIMBING
    @Inject(at = @At("RETURN"), method = "isClimbing", cancellable = true)
    public void doSpiderClimbing(CallbackInfoReturnable<Boolean> info) {
        if(!info.getReturnValue()) {
            if((Entity)this instanceof LivingEntity) {
                List<ClimbingPower> climbingPowers = PowerHolderComponent.KEY.get((Entity)this).getPowers(ClimbingPower.class, true);
                // TODO: Rethink how "holding" is implemented
                if(climbingPowers.size() > 0) {
                    if(climbingPowers.stream().anyMatch(ClimbingPower::isActive)) {
                        BlockPos pos = getBlockPos();
                        this.climbingPos = Optional.of(pos);
                        //origins_lastClimbingPos = getPos();
                        info.setReturnValue(true);
                    } else if(isHoldingOntoLadder()) {
                        //if(origins_lastClimbingPos != null && isHoldingOntoLadder()) {
                            if(climbingPowers.stream().anyMatch(ClimbingPower::canHold)) {
                                    info.setReturnValue(true);
                            }
                        //}
                    }
                }
            }
        }
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

    @Inject(method = "getAttributeValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D", at = @At("RETURN"), cancellable = true)
    private void modifyAttributeValue(EntityAttribute attribute, CallbackInfoReturnable<Double> cir) {
        double originalValue = this.getAttributes().getValue(attribute);
        double modified = PowerHolderComponent.modify(this, ModifyAttributePower.class, (float)originalValue, p -> p.getAttribute() == attribute);
        if(originalValue != modified) {
            cir.setReturnValue(modified);
        }
    }

    @ModifyVariable(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isOnGround()Z", opcode = Opcodes.GETFIELD, ordinal = 2))
    private float modifySlipperiness(float original) {
        return PowerHolderComponent.modify(this, ModifySlipperinessPower.class, original, p -> p.doesApply(getWorld(), getVelocityAffectingPos()));
    }

    @Inject(method = "pushAway", at = @At("HEAD"), cancellable = true)
    private void preventPushing(Entity entity, CallbackInfo ci) {
        if(PowerHolderComponent.hasPower(this, PreventEntityCollisionPower.class, p -> p.doesApply(entity))
            || PowerHolderComponent.hasPower(entity, PreventEntityCollisionPower.class, p -> p.doesApply(this))) {
            ci.cancel();
        }
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
    private ItemStack modifyEatenItemStack(ItemStack original) {
        if((Object)this instanceof PlayerEntity) {
            return original;
        }
        List<ModifyFoodPower> mfps = PowerHolderComponent.getPowers(this, ModifyFoodPower.class);
        mfps = mfps.stream().filter(mfp -> mfp.doesApply(original)).collect(Collectors.toList());
        ItemStack newStack = original;
        for(ModifyFoodPower mfp : mfps) {
            newStack = mfp.getConsumedItemStack(newStack);
        }
        ((ModifiableFoodEntity)this).setCurrentModifyFoodPowers(mfps);
        ((ModifiableFoodEntity)this).setOriginalFoodStack(original);
        return newStack;
    }

    @ModifyVariable(method = "eatFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyFoodEffects(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)V", shift = At.Shift.AFTER))
    private ItemStack unmodifyEatenItemStack(ItemStack modified) {
        ModifiableFoodEntity foodEntity = (ModifiableFoodEntity)this;
        ItemStack original = foodEntity.getOriginalFoodStack();
        if(original != null) {
            foodEntity.setOriginalFoodStack(null);
            return original;
        }
        return modified;
    }

    @Inject(method = "eatFood", at = @At("TAIL"))
    private void removeCurrentModifyFoodPowers(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        setCurrentModifyFoodPowers(new LinkedList<>());
    }

    @Redirect(method = "eatFood", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyFoodEffects(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)V"))
    private void preventApplyingFoodEffects(LivingEntity livingEntity, ItemStack stack, World world, LivingEntity targetEntity) {
        if(getCurrentModifyFoodPowers().stream().anyMatch(ModifyFoodPower::doesPreventEffects)) {
            return;
        }
        this.applyFoodEffects(stack, world, targetEntity);
    }

    @Shadow @Nullable private LivingEntity attacker;

    @Shadow protected abstract void applyFoodEffects(ItemStack stack, World world, LivingEntity targetEntity);

    @Shadow protected abstract void damageArmor(DamageSource source, float amount);

    @Shadow public abstract int getArmor();

    @Shadow public abstract double getAttributeValue(EntityAttribute attribute);

    @Shadow public abstract AttributeContainer getAttributes();

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
    public List<ModifyFoodPower> getCurrentModifyFoodPowers() {
        return apoli$currentModifyFoodPowers;
    }

    @Override
    public void setCurrentModifyFoodPowers(List<ModifyFoodPower> powers) {
        apoli$currentModifyFoodPowers = powers;
    }

    @Override
    public ItemStack getOriginalFoodStack() {
        return apoli$originalFoodStack;
    }

    @Override
    public void setOriginalFoodStack(ItemStack original) {
        apoli$originalFoodStack = original;
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void updateItemStackHolder(CallbackInfo ci) {
        InventoryUtil.forEachStack(this, stack -> ((EntityLinkedItemStack) stack).setEntity(this), stack -> ((EntityLinkedItemStack) stack).setEntity(this));
    }
}
