package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.access.MutableItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements MutableItemStack, EntityLinkedItemStack {

    @Shadow @Deprecated private Item item;

    @Shadow private NbtCompound nbt;

    @Shadow private int count;

    @Shadow public abstract int getMaxUseTime();

    @Shadow public abstract @Nullable Entity getHolder();

    @Shadow public abstract void setHolder(@Nullable Entity holder);

    @Unique
    private ItemStack apoli$usedItemStack;

    @Unique
    private Entity apoli$holdingEntity;

    @Override
    public Entity getEntity() {
        return getEntity(true);
    }

    @Override
    public Entity getEntity(boolean prioritiseVanillaHolder) {
        Entity vanillaHolder = getHolder();
        if(!prioritiseVanillaHolder || vanillaHolder == null) {
            return apoli$holdingEntity;
        }
        return vanillaHolder;
    }

    @Override
    public void setEntity(Entity entity) {
        this.apoli$holdingEntity = entity;
    }

    @Inject(method = "copy", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setBobbingAnimationTime(I)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void copyNewParams(CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack) {
        if (this.apoli$holdingEntity != null) {
            ((EntityLinkedItemStack)itemStack).setEntity(apoli$holdingEntity);
        }
    }

    @Inject(method = "finishUsing", at = @At("HEAD"))
    public void callActionOnUseFinishBefore(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        apoli$usedItemStack = ((ItemStack)(Object)this).copy();
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, apoli$usedItemStack,
                    ActionOnItemUsePower.TriggerType.FINISH, ActionOnItemUsePower.PriorityPhase.BEFORE);
        }
    }

    @Inject(method = "finishUsing", at = @At("RETURN"))
    public void callActionOnUseFinishAfter(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, cir.getReturnValue(), apoli$usedItemStack,
                    ActionOnItemUsePower.TriggerType.FINISH, ActionOnItemUsePower.PriorityPhase.AFTER);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void callActionOnUseInstantBefore(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if(user != null) {
            PowerHolderComponent component = PowerHolderComponent.KEY.get(user);
            ItemStack stackInHand = user.getStackInHand(hand);
            for(PreventItemUsePower piup : component.getPowers(PreventItemUsePower.class)) {
                if(piup.doesPrevent(stackInHand)) {
                    cir.setReturnValue(TypedActionResult.fail(stackInHand));
                    return;
                }
            }

            if(getMaxUseTime() == 0) {
                ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, (ItemStack)(Object)this,
                        ActionOnItemUsePower.TriggerType.INSTANT, ActionOnItemUsePower.PriorityPhase.BEFORE);
            } else {
                ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, (ItemStack)(Object)this,
                        ActionOnItemUsePower.TriggerType.START, ActionOnItemUsePower.PriorityPhase.BEFORE);
            }
        }
    }

    @Inject(method = "use", at = @At("RETURN"))
    private void callActionOnUseInstantAfter(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if(user != null) {
            ActionResult ar = cir.getReturnValue().getResult();
            if(!ar.isAccepted()) {
                return;
            }
            if(getMaxUseTime() == 0) {
                ActionOnItemUsePower.executeActions(user, cir.getReturnValue().getValue(), cir.getReturnValue().getValue(),
                        ActionOnItemUsePower.TriggerType.INSTANT, ActionOnItemUsePower.PriorityPhase.AFTER);
            } else {
                ActionOnItemUsePower.executeActions(user, cir.getReturnValue().getValue(), cir.getReturnValue().getValue(),
                        ActionOnItemUsePower.TriggerType.START, ActionOnItemUsePower.PriorityPhase.AFTER);
            }
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    private void callActionOnUseStopBefore(World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, (ItemStack)(Object)this,
                    ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.BEFORE);
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("RETURN"))
    private void callActionOnUseStopAfter(World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, (ItemStack)(Object)this,
                    ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.AFTER);
        }
    }

    @Inject(method = "usageTick", at = @At("HEAD"))
    private void callActionOnUseDuringBefore(World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, (ItemStack)(Object)this,
                    ActionOnItemUsePower.TriggerType.DURING, ActionOnItemUsePower.PriorityPhase.BEFORE);
        }
    }

    @Inject(method = "usageTick", at = @At("RETURN"))
    private void callActionOnUseDuringAfter(World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, (ItemStack)(Object)this,
                    ActionOnItemUsePower.TriggerType.DURING, ActionOnItemUsePower.PriorityPhase.AFTER);
        }
    }

    @Override
    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public void setFrom(ItemStack stack) {
        setItem(stack.getItem());
        nbt = stack.getNbt();
        count = stack.getCount();
        setEntity(((EntityLinkedItemStack)stack).getEntity(false));
    }
}
