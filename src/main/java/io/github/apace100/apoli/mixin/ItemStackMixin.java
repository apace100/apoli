package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.access.PotentiallyEdibleItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnItemUsePower;
import io.github.apace100.apoli.power.EdibleItemPower;
import io.github.apace100.apoli.power.PreventItemUsePower;
import io.github.apace100.apoli.util.InventoryUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
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

import java.util.Comparator;
import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements EntityLinkedItemStack, PotentiallyEdibleItemStack {

    @Shadow public abstract int getMaxUseTime();

    @Shadow public abstract @Nullable Entity getHolder();

    @Shadow public abstract Item getItem();

    @Shadow public abstract boolean isEmpty();

    @Shadow public abstract ItemStack copy();

    @Unique
    private Entity apoli$holdingEntity;

    @Override
    public Entity apoli$getEntity() {
        return apoli$getEntity(true);
    }

    @Override
    public Entity apoli$getEntity(boolean prioritiseVanillaHolder) {
        Entity vanillaHolder = getHolder();
        if(!prioritiseVanillaHolder || vanillaHolder == null) {
            return apoli$holdingEntity;
        }
        return vanillaHolder;
    }

    @Override
    public void apoli$setEntity(Entity entity) {
        this.apoli$holdingEntity = entity;
    }

    @Override
    public Optional<FoodComponent> apoli$getFoodComponent() {
        return apoli$getEdiblePower().map(EdibleItemPower::getFoodComponent);
    }

    @Unique
    private Optional<EdibleItemPower> apoli$getEdiblePower() {

        EdibleItemPower edibleItemPower = PowerHolderComponent.getPowers(apoli$getEntity(), EdibleItemPower.class)
            .stream()
            .filter(p -> p.doesApply((ItemStack) (Object) this))
            .max(Comparator.comparing(EdibleItemPower::getPriority))
            .orElse(null);

        if (edibleItemPower == null || (this.getItem().isFood() && edibleItemPower.getPriority() <= 0)) {
            return Optional.empty();
        }

        return Optional.of(edibleItemPower);

    }

    @Inject(method = "copy", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setBobbingAnimationTime(I)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void copyNewParams(CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack) {
        if (this.apoli$holdingEntity != null) {
            ((EntityLinkedItemStack)itemStack).apoli$setEntity(apoli$holdingEntity);
        }
    }

    @Inject(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    public void callActionOnUseFinishBefore(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir, @Share("finishStackRef") LocalRef<StackReference> ref) {
        if(user != null) {
            StackReference reference = InventoryUtil.getStackReferenceFromStack(user, (ItemStack)(Object)this);
            ActionOnItemUsePower.executeActions(user, reference, reference.get(),
                    ActionOnItemUsePower.TriggerType.FINISH, ActionOnItemUsePower.PriorityPhase.BEFORE);
            ref.set(reference);
        }
    }

    @WrapOperation(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack callActionOnUseFinishAfter(Item instance, ItemStack stack, World world, LivingEntity user, Operation<ItemStack> original, @Share("finishStackRef") LocalRef<StackReference> ref) {
        if(user != null && ref.get() != StackReference.EMPTY) {
            StackReference reference = InventoryUtil.getStackReferenceFromStack(user, (ItemStack)(Object)this);
            ActionOnItemUsePower.executeActions(user, ref.get(), ref.get().get(),
                    ActionOnItemUsePower.TriggerType.FINISH, ActionOnItemUsePower.PriorityPhase.AFTER);
            reference.set(ref.get().get());
            return original.call(reference.get().getItem(), reference.get(), world, user);
        }
        return original.call(instance, stack, world, user);
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
        }
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    private void callActionOnUseInstantBefore(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir, @Share("startStackRef") LocalRef<StackReference> ref) {
        if(user != null) {
            StackReference reference = InventoryUtil.createStackReference((ItemStack)(Object)this);

            if(getMaxUseTime() == 0) {
                ActionOnItemUsePower.executeActions(user, reference, (ItemStack)(Object)this,
                        ActionOnItemUsePower.TriggerType.INSTANT, ActionOnItemUsePower.PriorityPhase.BEFORE);
            } else {
                ActionOnItemUsePower.executeActions(user, reference, (ItemStack)(Object)this,
                        ActionOnItemUsePower.TriggerType.START, ActionOnItemUsePower.PriorityPhase.BEFORE);
            }

            ref.set(reference);
        }
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private TypedActionResult<ItemStack> callActionOnUseInstantAfter(Item instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original, @Share("startStackRef") LocalRef<StackReference> ref) {
        if(user != null) {
            TypedActionResult<ItemStack> ar = original.call(ref.get().get().getItem(), world, user, hand);
            if(!ar.getResult().isAccepted()) {
                return ar;
            }
            if(getMaxUseTime() == 0) {
                ActionOnItemUsePower.executeActions(user, ref.get(), ref.get().get(),
                        ActionOnItemUsePower.TriggerType.INSTANT, ActionOnItemUsePower.PriorityPhase.AFTER);
            } else {
                ActionOnItemUsePower.executeActions(user, ref.get(), ref.get().get(),
                        ActionOnItemUsePower.TriggerType.START, ActionOnItemUsePower.PriorityPhase.AFTER);
            }
            return new TypedActionResult<>(ar.getResult(), ref.get().get());
        }
        return original.call(instance, world, user, hand);
    }

    @Inject(method = "onStoppedUsing", at = @At(value = "HEAD"))
    private void callActionOnUseStopBefore(World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, @Share("stopStackRef") LocalRef<StackReference> ref) {
        if(user != null) {
            StackReference reference = InventoryUtil.createStackReference((ItemStack)(Object)this);
            ActionOnItemUsePower.executeActions(user, reference, (ItemStack)(Object)this,
                    ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.BEFORE);
            ref.set(reference);
        }
    }

    @WrapOperation(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V"))
    private void callActionOnUseStopAfter(Item instance, ItemStack stack, World world, LivingEntity user, int remainingUseTicks, Operation<Void> original, @Share("stopStackRef") LocalRef<StackReference> ref) {
        if(user != null) {
            StackReference reference = InventoryUtil.getStackReferenceFromStack(user, (ItemStack) (Object) this);
            ActionOnItemUsePower.executeActions(user, ref.get(), ref.get().get(),
                    ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.AFTER);
            reference.set(ref.get().get());
            original.call(reference.get().getItem(), reference.get(), world, user, remainingUseTicks);
        } else {
            original.call(instance, stack, world, user, remainingUseTicks);
        }
    }

    @Inject(method = "usageTick", at = @At(value = "HEAD"))
    private void callActionOnUseDuringBefore(World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, @Share("duringStackRef") LocalRef<StackReference> ref) {
        if(user != null) {
            StackReference reference = InventoryUtil.createStackReference((ItemStack)(Object)this);
            ActionOnItemUsePower.executeActions(user, reference, (ItemStack)(Object)this,
                    ActionOnItemUsePower.TriggerType.DURING, ActionOnItemUsePower.PriorityPhase.BEFORE);
            ref.set(reference);
        }
    }

    @WrapOperation(method = "usageTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V"))
    private void callActionOnUseDuringAfter(Item instance, World world, LivingEntity user, ItemStack stack, int remainingUseTicks, Operation<Void> original, @Share("duringStackRef") LocalRef<StackReference> ref) {
        if(user != null) {
            StackReference reference = InventoryUtil.getStackReferenceFromStack(user, (ItemStack)(Object)this);
            ActionOnItemUsePower.executeActions(user, ref.get(), ref.get().get(),
                    ActionOnItemUsePower.TriggerType.DURING, ActionOnItemUsePower.PriorityPhase.AFTER);
            reference.set(ref.get().get());
            original.call(reference.get().getItem(), world, user, reference.get(), remainingUseTicks);
        } else {
            original.call(instance, world, user, stack, remainingUseTicks);
        }
    }

    @ModifyReturnValue(method = "getUseAction", at = @At("RETURN"))
    private UseAction apoli$replaceUseAction(UseAction original) {
        return apoli$getEdiblePower()
            .map(p -> p.getConsumeAnimation().getAction())
            .orElse(original);
    }

    @ModifyReturnValue(method = "getEatSound", at = @At("RETURN"))
    private SoundEvent apoli$replaceEatingSound(SoundEvent original) {
        return apoli$getEdiblePower()
            .map(EdibleItemPower::getConsumeSoundEvent)
            .orElse(original);
    }

    @ModifyReturnValue(method = "getDrinkSound", at = @At("RETURN"))
    private SoundEvent apoli$replaceDrinkingSound(SoundEvent original) {
        return apoli$getEdiblePower()
            .map(EdibleItemPower::getConsumeSoundEvent)
            .orElse(original);
    }

    @ModifyReturnValue(method = "getMaxUseTime", at = @At("RETURN"))
    private int apoli$modifyMaxConsumingTime(int original) {
        return apoli$getEdiblePower()
            .map(EdibleItemPower::getConsumingTime)
            .orElse(original);
    }

    @WrapWithCondition(method = "usageTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V"))
    private boolean apoli$disableUsageTickOnConsumingCustomFood(Item instance, World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        return apoli$getEdiblePower().isEmpty();
    }

    @WrapWithCondition(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V"))
    private boolean apoli$disableOnStoppedUsingOnConsumingCustomFood(Item instance, ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        return apoli$getEdiblePower().isEmpty();
    }

    @WrapOperation(method = "isUsedOnRelease", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;isUsedOnRelease(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean apoli$useOnReleaseIfCustomFood(Item instance, ItemStack stack, Operation<Boolean> original) {
        return apoli$getEdiblePower().isEmpty() ? original.call(instance, stack) : false;
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private TypedActionResult<ItemStack> apoli$consumeCustomFood(Item instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original) {

        EdibleItemPower edibleItemPower = apoli$getEdiblePower().orElse(null);
        if (edibleItemPower == null) {
            return original.call(instance, world, user, hand);
        }

        ItemStack stackInHand = user.getStackInHand(hand);
        if (!user.canConsume(edibleItemPower.getFoodComponent().isAlwaysEdible())) {
            return original.call(instance, world, user, hand);
        }

        user.setCurrentHand(hand);
        return TypedActionResult.consume(stackInHand);

    }

    @WrapOperation(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack apoli$finishConsumingCustomFood(Item instance, ItemStack stack, World world, LivingEntity user, Operation<ItemStack> original) {

        EdibleItemPower edibleItemPower = apoli$getEdiblePower().orElse(null);
        if (edibleItemPower == null) {
            return original.call(instance, stack, world, user);
        }

        edibleItemPower.applyEffects();
        edibleItemPower.executeEntityAction();

        StackReference newReference = InventoryUtil.createStackReference(user.eatFood(world, this.copy()));
        StackReference resultReference = edibleItemPower.executeItemActions(newReference);

        tryOfferingResultStack:
        if (resultReference != StackReference.EMPTY) {

            if (newReference.get().isEmpty()) {
                return resultReference.get();
            }

            if (ItemStack.canCombine(resultReference.get(), newReference.get())) {
                newReference.get().increment(1);
                break tryOfferingResultStack;
            }

            if (user instanceof PlayerEntity playerEntity && !playerEntity.isCreative()) {
                playerEntity.getInventory().offerOrDrop(resultReference.get());
                break tryOfferingResultStack;
            }

            if (!(user instanceof PlayerEntity)) {
                InventoryUtil.throwItem(user, resultReference.get(), false, false);
            }

        }

        return newReference.get();

    }
}
