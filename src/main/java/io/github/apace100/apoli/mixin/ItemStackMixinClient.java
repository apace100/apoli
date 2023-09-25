package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.PreventItemUsePower;
import io.github.apace100.apoli.power.TooltipPower;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.github.apace100.apoli.util.KeyBindingUtil;
import io.github.apace100.apoli.util.StackPowerUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.LinkedList;
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

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0, shift = At.Shift.AFTER))
    private void apoli$addUnusableTooltip(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, @Local List<Text> tooltip) {

        if (player == null) {
            return;
        }

        ApoliConfigClient.Tooltips config = ((ApoliConfigClient) Apoli.config).tooltips;
        if (!config.showUsabilityHints || !isSectionVisible(getHideFlags(), ItemStack.TooltipSection.ADDITIONAL)) {
            return;
        }

        List<PreventItemUsePower> powers = PowerHolderComponent.getPowers(player, PreventItemUsePower.class)
            .stream()
            .filter(p -> p.doesPrevent((ItemStack) (Object) this))
            .toList();
        if (powers.isEmpty()) {
            return;
        }

        String translationKey = "tooltip.apoli.unusable." + getUseAction().toString().toLowerCase(Locale.ROOT) + (powers.size() == 1 ? ".single" : ".multiple");

        Formatting baseTextColor = Formatting.GRAY;
        Formatting powerTextColor = Formatting.RED;

        Text baseText;
        Text powerText;

        if (config.compactUsabilityHints || powers.size() == 1) {

            if (powers.size() == 1) {

                PreventItemUsePower power = powers.get(0);

                powerText = power.getType().getName().formatted(powerTextColor);
                baseText = Text.translatable(translationKey, powerText).formatted(baseTextColor);

                tooltip.add(baseText);

            } else {

                MinecraftClient client = MinecraftClient.getInstance();
                KeyBinding keyBinding = ApoliClient.showPowersOnUsabilityHint;

                Integer keyCode = keyBinding.isUnbound() ? null : InputUtil.fromTranslationKey(keyBinding.getBoundKeyTranslationKey()).getCode();
                boolean isKeyPressed = keyCode != null && InputUtil.isKeyPressed(client.getWindow().getHandle(), keyCode);

                if (isKeyPressed) {
                    apoli$addExpandedTooltip(powers, tooltip, translationKey, powerTextColor, baseTextColor);
                } else {

                    powerText = Text.translatable("tooltip.apoli.usability_hint.power_count", powers.size()).formatted(powerTextColor);
                    baseText = Text.translatable(translationKey, powerText).formatted(baseTextColor);

                    tooltip.add(baseText);
                    tooltip.add(Text.empty());

                    Text keybindText = KeyBindingUtil.getLocalizedName(keyBinding.getTranslationKey()).styled(style -> style
                        .withColor(Formatting.YELLOW)
                        .withItalic(keyBinding.isUnbound())
                    );

                    Text guideText = Text.translatable("tooltip.apoli.usability_hint.show_powers", keybindText).formatted(baseTextColor);
                    tooltip.add(guideText);

                }

            }
        } else {
            apoli$addExpandedTooltip(powers, tooltip, translationKey, powerTextColor, baseTextColor);
        }

    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasNbt()Z", ordinal = 1))
    private void apoli$addStackPowerTooltips(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, @Local List<Text> texts) {

        for (EquipmentSlot slot : EquipmentSlot.values()) {

            List<StackPowerUtil.StackPower> stackPowers = StackPowerUtil.getPowers((ItemStack) (Object) this, slot)
                .stream()
                .filter(sp -> !sp.isHidden)
                .toList();
            if (stackPowers.isEmpty()) {
                continue;
            }

            texts.add(Text.empty());
            texts.add(Text.translatable("item.modifiers." + slot.getName()).formatted(Formatting.GRAY));

            for (StackPowerUtil.StackPower stackPower : stackPowers) {

                if (!PowerTypeRegistry.contains(stackPower.powerId)) {
                    continue;
                }

                PowerType<?> powerType = PowerTypeRegistry.get(stackPower.powerId);
                Text powerNameText = Text
                    .literal(" ")
                    .append(powerType.getName())
                    .formatted(stackPower.isNegative ? Formatting.RED : Formatting.BLUE);
                texts.add(powerNameText);

                if (!context.isAdvanced()) {
                    continue;
                }

                Text powerDescriptionText = Text
                    .literal("  ")
                    .append(powerType.getDescription())
                    .formatted(Formatting.GRAY);
                texts.add(powerDescriptionText);

            }

        }

        PowerHolderComponent.getPowers(player, TooltipPower.class)
            .stream()
            .filter(p -> p.doesApply((ItemStack) (Object) this))
            .sorted(Comparator.comparing(TooltipPower::getOrder))
            .forEachOrdered(p -> p.addToTooltip(texts));

    }

    @Unique
    private void apoli$addExpandedTooltip(List<PreventItemUsePower> powers, List<Text> tooltip, String translationKey, Formatting powerTextColor, Formatting baseTextColor) {

        List<Text> powerTexts = new LinkedList<>();
        for (PreventItemUsePower power : powers) {

            MutableText prependedText = Text.literal("  - ").formatted(baseTextColor);
            MutableText powerText = power.getType().getName().formatted(powerTextColor);

            powerTexts.add(prependedText.append(powerText));

        }

        Text powerText = Text.translatable("tooltip.apoli.usability_hint.power_count", powers.size()).formatted(powerTextColor);
        Text baseText = Text.translatable(translationKey, powerText).formatted(baseTextColor);

        tooltip.add(baseText);
        tooltip.addAll(powerTexts);

    }

}
