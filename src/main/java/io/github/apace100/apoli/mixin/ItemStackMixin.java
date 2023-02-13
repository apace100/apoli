package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MutableItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.github.apace100.apoli.util.StackPowerUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
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

    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info) {
        if(user != null) {
            PowerHolderComponent component = PowerHolderComponent.KEY.get(user);
            ItemStack stackInHand = user.getStackInHand(hand);
            for(PreventItemUsePower piup : component.getPowers(PreventItemUsePower.class)) {
                if(piup.doesPrevent(stackInHand)) {
                    info.setReturnValue(TypedActionResult.fail(stackInHand));
                    break;
                }
            }
        }
    }

    @Unique
    private ItemStack apoli$usedItemStack;

    @Inject(method = "finishUsing", at = @At("HEAD"))
    public void cacheUsedItemStack(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        apoli$usedItemStack = ((ItemStack)(Object)this).copy();
    }

    @Inject(method = "finishUsing", at = @At("RETURN"))
    public void callActionOnUse(World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if(user instanceof PlayerEntity) {
            ItemStack returnStack = cir.getReturnValue();
            PowerHolderComponent component = PowerHolderComponent.KEY.get(user);
            for(ActionOnItemUsePower p : component.getPowers(ActionOnItemUsePower.class)) {
                if(p.doesApply(apoli$usedItemStack)) {
                    p.executeActions(returnStack);
                }
            }
        }
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    public void apoli$setHolderOnTick(World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (!hasHolder()) ((MutableItemStack) this).setHolder(entity);
    }

    @Inject(method = "copy", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setBobbingAnimationTime(I)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void apoli$setHolderOnCopy(CallbackInfoReturnable<ItemStack> cir, ItemStack itemStack) {
        if (!hasHolder()) ((MutableItemStack) itemStack).setHolder(getHolder());
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

    @Unique
    private Entity apoli$holder;

    @Override
    public boolean hasHolder() {
        return apoli$holder != null;
    }

    @Override
    public Entity getHolder() {
        return apoli$holder;
    }

    @Override
    public ItemStack setHolder(Entity entity) {
        if (!hasHolder()) apoli$holder = entity;
        return (ItemStack) (Object) this;
    }

}
