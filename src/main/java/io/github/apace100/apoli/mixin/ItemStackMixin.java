package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.PriorityPhase;
import io.github.apace100.apoli.util.StackClickPhase;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.*;
import net.minecraft.item.ItemUsage;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements EntityLinkedItemStack, FabricItemStack {

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

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private TypedActionResult<ItemStack> apoli$onItemUse(Item item, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> original) {

        //  region  Prevent item use
        ItemStack thisAsStack = (ItemStack) (Object) this;
        if (PowerHolderComponent.hasPower(user, PreventItemUsePower.class, piup -> piup.doesPrevent(thisAsStack))) {
            return TypedActionResult.fail(thisAsStack);
        }
        //  endregion

        //  region  Action on item before use
        StackReference useStackReference = InventoryUtil.getStackReferenceFromStack(user, thisAsStack);
        ItemStack useStack = useStackReference.get();

        ActionOnItemUsePower.TriggerType triggerType = useStack.getMaxUseTime() == 0
            ? ActionOnItemUsePower.TriggerType.INSTANT
            : ActionOnItemUsePower.TriggerType.START;
        ActionOnItemUsePower.executeActions(user, useStackReference, useStack, triggerType, PriorityPhase.BEFORE);
        //  endregion

        //  region  Edible item
        ItemStack oldUseStack = useStack.copy();
        boolean canConsumeCustomFood = EdibleItemPower.get(useStack, user)
            .map(EdibleItemPower::getFoodComponent)
            .map(fc -> user.canConsume(fc.isAlwaysEdible()))
            .orElse(false);

        TypedActionResult<ItemStack> action = canConsumeCustomFood
            ? ItemUsage.consumeHeldItem(world, user, hand)
            : original.call(useStack.getItem(), world, user, hand);

        if (!action.getResult().isAccepted()) {
            return action;
        }
        //  endregion

        //  region  Action on item after use
        useStackReference = StackReference.of(user, LivingEntity.getPreferredEquipmentSlot(oldUseStack));
        triggerType = useStack.getMaxUseTime() == 0
            ? ActionOnItemUsePower.TriggerType.INSTANT
            : ActionOnItemUsePower.TriggerType.START;

        ActionOnItemUsePower.executeActions(user, useStackReference, useStack, triggerType, PriorityPhase.AFTER);
        return action;
        //  endregion

    }

    @WrapOperation(method = "usageTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;usageTick(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;I)V"))
    private void apoli$actionOnItemDuringUse(Item item, World world, LivingEntity user, ItemStack stack, int remainingUseTicks, Operation<Void> original, @Share("usingStackReference") LocalRef<StackReference> sharedUsingStackReference) {

        ActionOnItemUsePower.TriggerType triggerType = ActionOnItemUsePower.TriggerType.DURING;

        StackReference usingStackReference = InventoryUtil.getStackReferenceFromStack(user, (ItemStack) (Object) this);
        ItemStack usingStack = usingStackReference.get();

        ActionOnItemUsePower.executeActions(user, usingStackReference, usingStack, triggerType, PriorityPhase.BEFORE);

        if (EdibleItemPower.get(usingStack, user).isEmpty()) {
            original.call(usingStack.getItem(), world, user, usingStack, remainingUseTicks);
        }

        else {
            ActionOnItemUsePower.executeActions(user, usingStackReference, usingStack, triggerType, PriorityPhase.AFTER);
        }

    }

    @WrapOperation(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V"))
    private void apoli$actionOnItemStoppedUsing(Item item, ItemStack stack, World world, LivingEntity user, int remainingUseTicks, Operation<Void> original, @Share("stoppedUsingStackReference") LocalRef<StackReference> sharedStoppedUsingStackReference) {

        ActionOnItemUsePower.TriggerType triggerType = ActionOnItemUsePower.TriggerType.STOP;

        StackReference stoppedUsingStackReference = InventoryUtil.getStackReferenceFromStack(user, (ItemStack) (Object) this);
        ItemStack stoppedUsingStack = stoppedUsingStackReference.get();

        ActionOnItemUsePower.executeActions(user, stoppedUsingStackReference, stoppedUsingStack, triggerType, PriorityPhase.BEFORE);

        if (EdibleItemPower.get(stoppedUsingStack, user).isEmpty()) {
            original.call(stoppedUsingStack.getItem(), stoppedUsingStack, world, user, remainingUseTicks);
        }

        else {
            ActionOnItemUsePower.executeActions(user, stoppedUsingStackReference, stoppedUsingStack, triggerType, PriorityPhase.AFTER);
        }

    }

    @WrapOperation(method = "finishUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack apoli$onFinishItemUse(Item item, ItemStack stack, World world, LivingEntity user, Operation<ItemStack> original) {

        //  region  Action on item before finish using
        StackReference finishUsingStackRef = InventoryUtil.getStackReferenceFromStack(user, stack);
        ItemStack finishUsingStack = finishUsingStackRef.get();

        ActionOnItemUsePower.executeActions(user, finishUsingStackRef, finishUsingStack, ActionOnItemUsePower.TriggerType.FINISH, PriorityPhase.BEFORE);
        //  endregion

        //  region  Edible item consumption effects
        finishUsingStackRef.set(EdibleItemPower.get(finishUsingStack, user)
            .map(p -> user.eatFood(world, stack))
            .orElseGet(() -> original.call(finishUsingStack.getItem(), finishUsingStack, world, user)));
        //  endregion

        //  region  Action on item after finish using
        ActionOnItemUsePower.executeActions(user, finishUsingStackRef, finishUsingStack, ActionOnItemUsePower.TriggerType.FINISH, PriorityPhase.AFTER);
        return finishUsingStack;
        //  endregion

    }

    @ModifyReturnValue(method = "getUseAction", at = @At("RETURN"))
    private UseAction apoli$replaceUseAction(UseAction original) {
        return EdibleItemPower.get((ItemStack) (Object) this)
            .map(p -> p.getConsumeAnimation().getAction())
            .orElse(original);
    }

    @ModifyReturnValue(method = "getEatSound", at = @At("RETURN"))
    private SoundEvent apoli$replaceEatingSound(SoundEvent original) {
        return EdibleItemPower.get((ItemStack) (Object) this)
            .map(EdibleItemPower::getConsumeSoundEvent)
            .orElse(original);
    }

    @ModifyReturnValue(method = "getDrinkSound", at = @At("RETURN"))
    private SoundEvent apoli$replaceDrinkingSound(SoundEvent original) {
        return EdibleItemPower.get((ItemStack) (Object) this)
            .map(EdibleItemPower::getConsumeSoundEvent)
            .orElse(original);
    }

    @ModifyReturnValue(method = "getMaxUseTime", at = @At("RETURN"))
    private int apoli$modifyMaxConsumingTime(int original) {
        return EdibleItemPower.get((ItemStack) (Object) this)
            .map(EdibleItemPower::getConsumingTime)
            .orElse(original);
    }

    @WrapOperation(method = "isUsedOnRelease", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;isUsedOnRelease(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean apoli$useOnReleaseIfCustomFood(Item item, ItemStack stack, Operation<Boolean> original) {
        return EdibleItemPower.get(stack).isEmpty()
            ? original.call(item, stack)
            : false;
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
