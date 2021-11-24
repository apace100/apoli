package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.HiddenEffectStatus;
import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.ModPackets;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.StackPowerUtil;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
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
                    ((HiddenEffectStatus) effect).getHiddenEffect()
            );
        }
        return effect;
    }

    @Inject(method = "setAttacker", at = @At("TAIL"))
    private void syncAttacker(LivingEntity attacker, CallbackInfo ci) {
        if(!world.isClient) {
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
    private void modifyWalkableFluids(Fluid fluid, CallbackInfoReturnable<Boolean> info) {
        if(PowerHolderComponent.getPowers(this, WalkOnFluidPower.class).stream().anyMatch(p -> fluid.isIn(p.getFluidTag()))) {
            info.setReturnValue(true);
        }
    }

    private boolean apoli$hasModifiedDamage;

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float modifyDamageTaken(float originalValue, DamageSource source, float amount) {
        float newValue = originalValue;
        LivingEntity thisAsLiving = (LivingEntity)(Object)this;
        if(source.getAttacker() != null && !source.isProjectile()) {
            newValue = PowerHolderComponent.modify(source.getAttacker(), ModifyDamageDealtPower.class, originalValue,
                p -> p.doesApply(source, originalValue, thisAsLiving), p -> p.executeActions(thisAsLiving));
        }

        float intermediateValue = newValue;
        newValue = PowerHolderComponent.modify(this, ModifyDamageTakenPower.class,
            intermediateValue, p -> p.doesApply(source, intermediateValue), p -> p.executeActions(source.getAttacker()));

        apoli$hasModifiedDamage = newValue != originalValue;

        return newValue;
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isSleeping()Z"), cancellable = true)
    private void preventHitIfDamageIsZero(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(apoli$hasModifiedDamage && amount == 0.0F) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void invokeHitActions(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue()) {
            Entity attacker = source.getAttacker();
            if(attacker != null) {
                PowerHolderComponent.withPower(this, ActionWhenHitPower.class, p -> true, p -> p.whenHit(attacker, source, amount));
                PowerHolderComponent.withPower(attacker, ActionOnHitPower.class, p -> true, p -> p.onHit(this, source, amount));
            }
            PowerHolderComponent.getPowers(this, SelfActionWhenHitPower.class).forEach(p -> p.whenHit(source, amount));
            PowerHolderComponent.getPowers(this, AttackerActionWhenHitPower.class).forEach(p -> p.whenHit(source, amount));
            PowerHolderComponent.getPowers(source.getAttacker(), SelfActionOnHitPower.class).forEach(p -> p.onHit((LivingEntity)(Object)this, source, amount));
            PowerHolderComponent.getPowers(source.getAttacker(), TargetActionOnHitPower.class).forEach(p -> p.onHit((LivingEntity)(Object)this, source, amount));

        }
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;onDeath(Lnet/minecraft/entity/damage/DamageSource;)V"))
    private void invokeKillAction(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PowerHolderComponent.getPowers(source.getAttacker(), SelfActionOnKillPower.class).forEach(p -> p.onKill((LivingEntity)(Object)this, source, amount));
    }

    // ModifyLavaSpeedPower
    @ModifyConstant(method = "travel", constant = {
        @Constant(doubleValue = 0.5D, ordinal = 0),
        @Constant(doubleValue = 0.5D, ordinal = 1),
        @Constant(doubleValue = 0.5D, ordinal = 2)
    })
    private double modifyLavaSpeed(double original) {
        return PowerHolderComponent.modify(this, ModifyLavaSpeedPower.class, original);
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
    @Inject(at = @At("HEAD"), method = "getJumpVelocity", cancellable = true)
    private void modifyJumpVelocity(CallbackInfoReturnable<Float> info) {
        float base = 0.42F * this.getJumpVelocityMultiplier();
        float modified = PowerHolderComponent.modify(this, ModifyJumpPower.class, base, p -> {
            p.executeAction();
            return true;
        });
        info.setReturnValue(modified);
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

    // SWIM_SPEED
    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;updateVelocity(FLnet/minecraft/util/math/Vec3d;)V", ordinal = 0))
    public void modifyUnderwaterMovementSpeed(LivingEntity livingEntity, float speedMultiplier, Vec3d movementInput) {
        livingEntity.updateVelocity(PowerHolderComponent.modify(livingEntity, ModifySwimSpeedPower.class, speedMultiplier), movementInput);
    }

    @ModifyConstant(method = "swimUpward", constant = @Constant(doubleValue = 0.03999999910593033D))
    public double modifyUpwardSwimming(double original, Tag<Fluid> fluid) {
        if(fluid == FluidTags.LAVA) {
            return original;
        }
        return PowerHolderComponent.modify(this, ModifySwimSpeedPower.class, original);
    }

    @Environment(EnvType.CLIENT)
    @ModifyConstant(method = "knockDownwards", constant = @Constant(doubleValue = -0.03999999910593033D))
    public double swimDown(double original) {
        return PowerHolderComponent.modify(this, ModifySwimSpeedPower.class, original);
    }

    // SLOW_FALLING
    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/FluidState;"), method = "travel", name = "d", ordinal = 0)
    public double modifyFallingVelocity(double in) {
        List<ModifyFallingPower> modifyFallingPowers = PowerHolderComponent.getPowers(this, ModifyFallingPower.class);
        if(modifyFallingPowers.size() > 0) {
            ModifyFallingPower power = modifyFallingPowers.get(0);
            if(!power.takeFallDamage) {
                this.fallDistance = 0;
            }
            if(this.getVelocity().y <= 0.0D) {
                return power.velocity;
            }
        }
        return in;
    }

    @ModifyVariable(method = "travel", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/LivingEntity;onGround:Z", opcode = Opcodes.GETFIELD, ordinal = 2))
    private float modifySlipperiness(float original) {
        return PowerHolderComponent.modify(this, ModifySlipperinessPower.class, original, p -> p.doesApply(world, getVelocityAffectingPos()));
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

    @Shadow public float airStrafingSpeed;

    @Shadow @Nullable private LivingEntity attacker;

    @Shadow protected abstract void applyFoodEffects(ItemStack stack, World world, LivingEntity targetEntity);

    @Inject(method = "getMovementSpeed(F)F", at = @At("RETURN"), cancellable = true)
    private void modifyFlySpeed(float slipperiness, CallbackInfoReturnable<Float> cir){
        if (!onGround)
            cir.setReturnValue(PowerHolderComponent.modify(this, ModifyAirSpeedPower.class, airStrafingSpeed));
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
}
