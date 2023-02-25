package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MutableItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.github.apace100.apoli.util.StackPowerUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
import java.util.List;
import java.util.Locale;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements MutableItemStack {

    @Shadow @Deprecated private Item item;

    @Shadow private NbtCompound nbt;

    @Shadow private int count;

    @Shadow public abstract UseAction getUseAction();

    @Shadow protected abstract int getHideFlags();

    @Shadow
    private static boolean isSectionVisible(int flags, ItemStack.TooltipSection tooltipSection) {
        return (flags & tooltipSection.getFlag()) == 0;
    }

    @Shadow public abstract int getMaxUseTime();

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addUnusableTooltip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        if(player != null) {
            ApoliConfigClient.Tooltips config = ((ApoliConfigClient) Apoli.config).tooltips;
            if(!config.showUsabilityHints || !isSectionVisible(getHideFlags(), ItemStack.TooltipSection.ADDITIONAL)) {
                return;
            }
            List<PreventItemUsePower> powers = PowerHolderComponent.getPowers(player, PreventItemUsePower.class).stream().filter(p -> p.doesPrevent((ItemStack)(Object)this)).toList();
            int powerCountWithHidden = powers.size();
            powers = powers.stream().filter(p -> !p.getType().isHidden()).toList();
            if(powerCountWithHidden == 0) {
                return;
            }
            String translationKeyBase = "tooltip.apoli.unusable." + getUseAction().name().toLowerCase(Locale.ROOT);
            Formatting textColor = Formatting.GRAY;
            Formatting powerColor = Formatting.RED;
            if(config.compactUsabilityHints || powers.size() == 0) {
                if(powers.size() == 1) {
                    PreventItemUsePower power = powers.get(0);
                    MutableText preventText = Text.translatable(translationKeyBase + ".single",
                        power.getType().getName().formatted(powerColor)).formatted(textColor);
                    list.add(preventText);
                } else {
                    list.add(
                        Text.translatable(translationKeyBase + ".multiple",
                            Text.literal((powers.size() == 0 ? powerCountWithHidden : powers.size()) + "").formatted(powerColor))
                            .formatted(textColor));
                }
            } else {
                MutableText powerNameList = powers.get(0).getType().getName().formatted(powerColor);
                for(int i = 1; i < powers.size(); i++) {
                    powerNameList = powerNameList.append(Text.literal(", ").formatted(textColor));
                    powerNameList = powerNameList.append(powers.get(i).getType().getName().formatted(powerColor));
                }
                MutableText preventText = Text.translatable(translationKeyBase + ".single",
                    powerNameList).formatted(textColor);
                list.add(preventText);
            }
        }
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasNbt()Z", ordinal = 1), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addEquipmentPowerTooltips(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        for(EquipmentSlot slot : EquipmentSlot.values()) {
            List<StackPowerUtil.StackPower> powers = StackPowerUtil.getPowers((ItemStack)(Object)this, slot)
                .stream()
                .filter(sp -> !sp.isHidden)
                .toList();
            if(powers.size() > 0) {
                list.add(Text.empty());
                list.add((Text.translatable("item.modifiers." + slot.getName())).formatted(Formatting.GRAY));
                powers.forEach(sp -> {

                    if(PowerTypeRegistry.contains(sp.powerId)) {
                        PowerType<?> powerType = PowerTypeRegistry.get(sp.powerId);
                        list.add(
                            Text.literal(" ")
                                .append(powerType.getName())
                                .formatted(sp.isNegative ? Formatting.RED : Formatting.BLUE));
                        if(context.isAdvanced()) {
                            list.add(
                                Text.literal("  ")
                                    .append(powerType.getDescription())
                                    .formatted(Formatting.GRAY));
                        }
                    }
                });
            }
        }
        PowerHolderComponent.getPowers(player, TooltipPower.class)
            .stream().filter(t -> t.doesApply((ItemStack) (Object)this))
            .sorted(Comparator.comparing(TooltipPower::getOrder))
            .forEachOrdered(t -> t.addToTooltip(list));
    }

    @Unique
    private ItemStack usedItemStack;

    @Inject(method = "finishUsing", at = @At("HEAD"))
    public void callActionOnUseFinishBefore(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        usedItemStack = ((ItemStack)(Object)this).copy();
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, (ItemStack)(Object)this, usedItemStack,
                    ActionOnItemUsePower.TriggerType.FINISH, ActionOnItemUsePower.PriorityPhase.BEFORE);
        }
    }

    @Inject(method = "finishUsing", at = @At("RETURN"))
    public void callActionOnUseFinishAfter(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if(user != null) {
            ActionOnItemUsePower.executeActions(user, cir.getReturnValue(), usedItemStack,
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
    }
}
