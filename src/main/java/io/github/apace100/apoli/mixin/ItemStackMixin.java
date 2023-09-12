package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.access.MutableItemStack;
import io.github.apace100.apoli.access.PotentiallyEdibleItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnItemUsePower;
import io.github.apace100.apoli.power.EdibleItemPower;
import io.github.apace100.apoli.power.PreventItemUsePower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
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
public abstract class ItemStackMixin implements MutableItemStack, EntityLinkedItemStack, PotentiallyEdibleItemStack {

    @Shadow @Deprecated private Item item;

    @Shadow private NbtCompound nbt;

    @Shadow private int count;

    @Shadow public abstract int getMaxUseTime();

    @Shadow public abstract @Nullable Entity getHolder();

    @Shadow public abstract Item getItem();

    @Shadow public abstract boolean isEmpty();

    @Shadow public abstract ItemStack copy();

    @Unique
    private ItemStack apoli$usedItemStack;

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
        return apoli$getEdiblePower()
            .map(EdibleItemPower::getFoodComponent)
            .or(() -> Optional.ofNullable(this.getItem().getFoodComponent()));
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
            .map(p -> p.getFoodComponent().isSnack() ? 16 : 32)
            .orElse(original);
    }

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private TypedActionResult<ItemStack> apoli$consumeEdibleItem(TypedActionResult<ItemStack> original, World world, PlayerEntity user, Hand hand) {

        Optional<EdibleItemPower> edibleItemPower = apoli$getEdiblePower();
        if (edibleItemPower.isEmpty()) {
            return original;
        }

        ItemStack handStack = user.getStackInHand(hand);
        if (user.canConsume(edibleItemPower.get().getFoodComponent().isAlwaysEdible())) {
            user.setCurrentHand(hand);
        }

        return TypedActionResult.consume(handStack);

    }

    @ModifyReturnValue(method = "finishUsing", at = @At("RETURN"))
    private ItemStack apoli$afterConsuming(ItemStack original, World world, LivingEntity user) {

        EdibleItemPower edibleItemPower = apoli$getEdiblePower().orElse(null);
        if (edibleItemPower == null) {
            return original;
        }

        edibleItemPower.applyEffects();
        edibleItemPower.executeEntityAction();

        ItemStack newStack = user.eatFood(world, this.copy());
        ItemStack resultStack = null;

        boolean matching = false;

        tryOfferingResultStack:
        if (user instanceof PlayerEntity playerEntity && !playerEntity.isCreative()) {

            resultStack = edibleItemPower.getResultStack();
            if (resultStack == null) {
                break tryOfferingResultStack;
            }

            edibleItemPower.executeItemAction(resultStack);
            matching = ItemStack.canCombine(resultStack, newStack);

            if (!matching) {
                playerEntity.getInventory().offerOrDrop(resultStack);
            }

        }

        if (!matching && (!(user instanceof PlayerEntity playerEntity) || !playerEntity.isCreative())) {
            newStack.decrement(1);
        }

        if (newStack.isEmpty() && resultStack != null) {
            return resultStack;
        }

        edibleItemPower.executeItemAction(newStack);
        return newStack;

    }

    @Override
    public void apoli$setItem(Item item) {
        this.item = item;
    }

    @Override
    public void apoli$setFrom(ItemStack stack) {
        apoli$setItem(stack.getItem());
        nbt = stack.getNbt();
        count = stack.getCount();
        apoli$setEntity(((EntityLinkedItemStack)stack).apoli$getEntity(false));
    }
}
