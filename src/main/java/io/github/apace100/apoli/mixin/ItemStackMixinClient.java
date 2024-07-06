package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PreventItemUsePower;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.github.apace100.apoli.util.KeyBindingUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

//  TODO: Append tooltips of the stack power item component -eggohito
@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class ItemStackMixinClient {

    @Shadow
    public abstract UseAction getUseAction();

    @Shadow public abstract ComponentMap getComponents();

    @Shadow public abstract Item getItem();

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/tooltip/TooltipType;isAdvanced()Z", ordinal = 1))
    private void apoli$test(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {

        List<Text> tooltip = cir.getReturnValue();
        ApoliConfigClient.Tooltips config = ((ApoliConfigClient) Apoli.config).tooltips;

        if (!config.showUsabilityHints || this.getComponents().contains(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)) {
            return;
        }

        List<PreventItemUsePower> preventItemUsePowers = PowerHolderComponent.getPowers(player, PreventItemUsePower.class)
            .stream()
            .filter(p -> p.doesPrevent((ItemStack) (Object) this))
            .toList();

        if (preventItemUsePowers.isEmpty()) {
            return;
        }

        if (!tooltip.isEmpty() && tooltip.getLast().getString().isEmpty()) {
            tooltip.add(Text.empty());
        }

        String translationKey = "tooltip.apoli.unusuable." + this.getUseAction().toString().toLowerCase(Locale.ROOT) + (preventItemUsePowers.size() == 1 ? ".single" : ".multiple");

        Formatting baseTextFormat = Formatting.GRAY;
        Formatting powerTextFormat = Formatting.RED;

        Text powerText;
        Text baseText;

        if (preventItemUsePowers.size() == 1) {

            PreventItemUsePower preventItemUsePower = preventItemUsePowers.getFirst();

            powerText = preventItemUsePower.getType().getName().formatted(powerTextFormat);
            baseText = Text.translatable(translationKey, powerText).formatted(baseTextFormat);

            tooltip.add(baseText);

        }

        else if (config.compactUsabilityHints) {

            MinecraftClient client = MinecraftClient.getInstance();
            KeyBinding keyBinding = ApoliClient.showPowersOnUsabilityHint;

            Integer keyCode = !keyBinding.isUnbound()
                ? InputUtil.fromTranslationKey(keyBinding.getBoundKeyTranslationKey()).getCode()
                : null;
            boolean isKeyPressed = keyCode != null
                && InputUtil.isKeyPressed(client.getWindow().getHandle(), keyCode);

            if (isKeyPressed) {
                this.apoli$addExpandedTooltip(preventItemUsePowers, tooltip, translationKey, powerTextFormat, powerTextFormat);
            }

            else {

                powerText = Text.translatable("tooltip.apoli.usability_hint.power_count", preventItemUsePowers.size()).formatted(powerTextFormat);
                baseText = Text.translatable(translationKey, powerText).formatted(baseTextFormat);

                tooltip.add(baseText);
                tooltip.add(Text.empty());

                Text keyBindingText = KeyBindingUtil.getLocalizedName(keyBinding.getTranslationKey()).styled(style -> style
                    .withColor(Formatting.YELLOW)
                    .withItalic(keyBinding.isUnbound()));

                Text guideText = Text.translatable("tooltip.apoli.usability_hint.show_powers", keyBindingText).formatted(baseTextFormat);
                tooltip.add(guideText);

            }

        }

        else {
            this.apoli$addExpandedTooltip(preventItemUsePowers, tooltip, translationKey, powerTextFormat, baseTextFormat);
        }

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
