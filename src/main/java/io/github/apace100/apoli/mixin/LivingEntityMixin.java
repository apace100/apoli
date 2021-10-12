package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.ModPackets;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.StackPowerUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
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

    @Inject(method = "getEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/AttributeContainer;removeModifiers(Lcom/google/common/collect/Multimap;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
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

    @Inject(method = "getEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/AttributeContainer;addTemporaryModifiers(Lcom/google/common/collect/Multimap;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
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

    @Shadow public float flyingSpeed;

    @Shadow @Nullable private LivingEntity attacker;

    @Inject(method = "getMovementSpeed(F)F", at = @At("RETURN"), cancellable = true)
    private void modifyFlySpeed(float slipperiness, CallbackInfoReturnable<Float> cir){
        if (!onGround)
            cir.setReturnValue(PowerHolderComponent.modify(this, ModifyAirSpeedPower.class, flyingSpeed));
    }
}
