package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MutableItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.PreventItemUsePower;
import io.github.apace100.apoli.power.TooltipPower;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.github.apace100.apoli.util.StackPowerUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.UseAction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class ItemStackMixinClient {

    @Shadow
    public abstract UseAction getUseAction();

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
}
