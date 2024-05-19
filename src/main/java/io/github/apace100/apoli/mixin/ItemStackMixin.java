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
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.PriorityPhase;
import io.github.apace100.apoli.util.StackClickPhase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @ModifyReturnValue(method = "copy", at = @At("RETURN"))
    private ItemStack apoli$passHolderOnCopy(ItemStack original) {

        Entity holder = this.apoli$getEntity();
        if (holder != null) {

            if (original.isEmpty()) {
                original = ModifyEnchantmentLevelPower.getOrCreateWorkableEmptyStack(holder);
            }

            else {
                ((EntityLinkedItemStack) original).apoli$setEntity(holder);
            }

        }

        return original;

    }

    @Inject(method = "finishUsing", at = @At("HEAD"))
    public void callActionOnUseFinishBefore(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir, @Share("finishedStackRef") LocalRef<StackReference> finishedStackRef) {

        if (user == null) {
            return;
        }

        StackReference stackReference = InventoryUtil.getStackReferenceFromStack(user, (ItemStack) (Object) this);
        ActionOnItemUsePower.executeActions(user, stackReference, (ItemStack) (Object) this,
            ActionOnItemUsePower.TriggerType.FINISH, ActionOnItemUsePower.PriorityPhase.BEFORE);

        finishedStackRef.set(stackReference);

    }

    @WrapOperation(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack callActionOnUseFinishAfter(Item instance, ItemStack stack, World world, LivingEntity user, Operation<ItemStack> original, @Share("finishedStackRef") LocalRef<StackReference> finishedStackRef) {

        StackReference cachedStackRef = finishedStackRef.get();
        if (user == null || cachedStackRef == StackReference.EMPTY) {
            return original.call(instance, stack, world, null);
        }

        ItemStack cachedStack = cachedStackRef.get();
        ActionOnItemUsePower.executeActions(user, cachedStackRef, cachedStack,
            ActionOnItemUsePower.TriggerType.FINISH, ActionOnItemUsePower.PriorityPhase.AFTER);

        return original.call(cachedStack.getItem(), cachedStack, world, user);

    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void preventItemUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {

        if (user == null) {
            return;
        }

        ItemStack stackInHand = user.getStackInHand(hand);
        if (PowerHolderComponent.hasPower(user, PreventItemUsePower.class, piup -> piup.doesPrevent(stackInHand))) {
            cir.setReturnValue(TypedActionResult.fail(stackInHand));
        }

    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;"))
    private Item callActionOnInstantBefore(ItemStack instance, Operation<Item> original, World world, PlayerEntity user, Hand hand, @Share("useStackRef") LocalRef<StackReference> useStackRef) {

        if (user == null) {
            return original.call(instance);
        }

        ItemStack thisAsStack = (ItemStack) (Object) this;
        StackReference stackReference = InventoryUtil.getStackReferenceFromStack(user, thisAsStack);

        ActionOnItemUsePower.TriggerType triggerType = this.getMaxUseTime() == 0
            ? ActionOnItemUsePower.TriggerType.INSTANT : ActionOnItemUsePower.TriggerType.START;
        ActionOnItemUsePower.executeActions(user, stackReference, thisAsStack,
            triggerType, ActionOnItemUsePower.PriorityPhase.BEFORE);

        useStackRef.set(stackReference);
        return stackReference.get().getItem();

    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private TypedActionResult<ItemStack> callActionOnInstantAfter(Item instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original, @Share("useStackRef") LocalRef<StackReference> useStackRef) {

        StackReference cachedStackRef = useStackRef.get();
        ItemStack cachedStack = cachedStackRef.get();

        if (user == null || cachedStackRef == StackReference.EMPTY) {
            return original.call(instance, world, user, hand);
        }

        ItemStack cachedStackCopy = cachedStack.copy();
        TypedActionResult<ItemStack> actionResult = original.call(instance, world, user, hand);

        if (!actionResult.getResult().isAccepted()) {
            return actionResult;
        }

        //  If the item can be equipped and swapped, replace the stack reference with
        //  the destination stack reference of the item
        EquipmentSlot equipmentSlot = LivingEntity.getPreferredEquipmentSlot(cachedStackCopy);
        if (equipmentSlot != EquipmentSlot.MAINHAND) {
            cachedStackRef = StackReference.of(user, equipmentSlot);
        }

        ActionOnItemUsePower.TriggerType triggerType = this.getMaxUseTime() == 0
            ? ActionOnItemUsePower.TriggerType.INSTANT : ActionOnItemUsePower.TriggerType.START;
        ActionOnItemUsePower.executeActions(user, cachedStackRef, cachedStackRef.get(),
            triggerType, ActionOnItemUsePower.PriorityPhase.AFTER);

        return actionResult;

    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    private void callActionOnUseStopBefore(World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, @Share("stoppedUsingStackRef") LocalRef<StackReference> stoppedUsingStackRef) {

        if (user == null) {
            return;
        }

        ItemStack thisAsStack = (ItemStack) (Object) this;
        StackReference stackReference = InventoryUtil.getStackReferenceFromStack(user, thisAsStack);

        ActionOnItemUsePower.executeActions(user, stackReference, thisAsStack,
            ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.BEFORE);

        stoppedUsingStackRef.set(stackReference);

    }

    @WrapOperation(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V"))
    private void callActionOnUseStopAfter(Item instance, ItemStack stack, World world, LivingEntity user, int remainingUseTicks, Operation<Void> original, @Share("stoppedUsingStackRef") LocalRef<StackReference> stoppedUsingStackRef) {

        StackReference cachedStackRef = stoppedUsingStackRef.get();
        if (user == null || cachedStackRef == StackReference.EMPTY) {
            original.call(instance, stack, world, user, remainingUseTicks);
            return;
        }

        ItemStack cachedStack = cachedStackRef.get();
        ActionOnItemUsePower.executeActions(user, cachedStackRef, cachedStack,
            ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.AFTER);

        original.call(cachedStack.getItem(), cachedStack, world, user, remainingUseTicks);

    }

    @Inject(method = "usageTick", at = @At(value = "HEAD"))
    private void callActionOnUseDuringBefore(World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, @Share("usingStackRef") LocalRef<StackReference> usingStackRef) {

        if (user == null) {
            return;
        }

        ItemStack thisAsStack = (ItemStack) (Object) this;
        StackReference stackReference = InventoryUtil.getStackReferenceFromStack(user, thisAsStack);

        ActionOnItemUsePower.executeActions(user, stackReference, thisAsStack,
            ActionOnItemUsePower.TriggerType.DURING, ActionOnItemUsePower.PriorityPhase.BEFORE);

        usingStackRef.set(stackReference);

    }

    @WrapOperation(method = "usageTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V"))
    private void callActionOnUseDuringAfter(Item instance, World world, LivingEntity user, ItemStack stack, int remainingUseTicks, Operation<Void> original, @Share("usingStackRef") LocalRef<StackReference> usingStackRef) {

        StackReference cachedStackRef = usingStackRef.get();
        if (user == null || cachedStackRef == StackReference.EMPTY) {
            original.call(instance, world, user, stack, remainingUseTicks);
            return;
        }

        ItemStack cachedStack = cachedStackRef.get();
        ActionOnItemUsePower.executeActions(user, cachedStackRef, cachedStack,
            ActionOnItemUsePower.TriggerType.DURING, ActionOnItemUsePower.PriorityPhase.AFTER);

        original.call(cachedStack.getItem(), world, user, cachedStack, remainingUseTicks);

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

    @WrapOperation(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult apoli$consumeUsableOnBlockCustomFood(Item instance, ItemUsageContext context, Operation<ActionResult> original) {

        PlayerEntity user = context.getPlayer();
        EdibleItemPower edibleItemPower = this.apoli$getEdiblePower().orElse(null);

        if (user == null || edibleItemPower == null || !user.canConsume(edibleItemPower.getFoodComponent().isAlwaysEdible())) {
            return original.call(instance, context);
        }

        user.setCurrentHand(context.getHand());
        return ActionResult.CONSUME_PARTIAL;

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

    @WrapOperation(method = "onStackClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;onStackClicked(Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/slot/Slot;Lnet/minecraft/util/ClickType;Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    private boolean apoli$itemOnItem_cursorStack(Item cursorItem, ItemStack cursorStack, Slot slot, ClickType clickType, PlayerEntity player, Operation<Boolean> original) {

        StackClickPhase clickPhase = StackClickPhase.CURSOR;

        StackReference cursorStackReference = ((ScreenHandlerAccessor) player.currentScreenHandler).callGetCursorStackReference();
        StackReference slotStackReference = StackReference.of(slot.inventory, slot.getIndex());

        return ItemOnItemPower.executeActions(player, PriorityPhase.BEFORE, clickPhase, clickType, slot, slotStackReference, cursorStackReference)
            || original.call(cursorStackReference.get().getItem(), cursorStackReference.get(), slot, clickType, player)
            || ItemOnItemPower.executeActions(player, PriorityPhase.AFTER, clickPhase, clickType, slot, slotStackReference, cursorStackReference);

    }

    @WrapOperation(method = "onClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;onClicked(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/screen/slot/Slot;Lnet/minecraft/util/ClickType;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/inventory/StackReference;)Z"))
    private boolean apoli$itemOnItem_slotStack(Item slotItem, ItemStack slotStack, ItemStack cursorStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference, Operation<Boolean> original) {

        StackClickPhase clickPhase = StackClickPhase.SLOT;
        StackReference slotStackReference = StackReference.of(slot.inventory, slot.getIndex());

        return ItemOnItemPower.executeActions(player, PriorityPhase.BEFORE, clickPhase, clickType, slot, slotStackReference, cursorStackReference)
            || original.call(slotStackReference.get().getItem(), slotStackReference.get(), cursorStackReference.get(), slot, clickType, player, cursorStackReference)
            || ItemOnItemPower.executeActions(player, PriorityPhase.AFTER, clickPhase, clickType, slot, slotStackReference, cursorStackReference);

    }

}
