package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.StackPowersComponent;
import io.github.apace100.apoli.power.PreventItemUsePower;
import io.github.apace100.apoli.power.TooltipPower;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.github.apace100.apoli.util.KeyBindingUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.ComponentHolder;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.UseAction;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class ItemStackMixinClient implements ComponentHolder {

    @Shadow
    public abstract UseAction getUseAction();

    @Shadow public abstract ComponentMap getComponents();

    @Shadow public abstract Item getItem();

    @Unique
    private final Set<AttributeModifierSlot> apoli$appendedSlots = new HashSet<>();

    @Unique
    private Item.TooltipContext apoli$tooltipContext;

    @Unique
    private TooltipType apoli$tooltipType;

    @Unique
    private List<Text> apoli$tooltip;

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/MutableText;append(Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"))
    private void apoli$cacheTooltipStuff(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir, @Local List<Text> tooltip) {
        this.apoli$appendedSlots.clear();
        this.apoli$tooltipContext = context;
        this.apoli$tooltipType = type;
        this.apoli$tooltip = tooltip;
    }

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;appendTooltip(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/item/tooltip/TooltipType;)V", shift = At.Shift.AFTER))
    private void apoli$appendUnusableTooltip(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir) {

        ApoliConfigClient.Tooltips config = ((ApoliConfigClient) Apoli.config).tooltips;
        if (!config.showUsabilityHints) {
            return;
        }

        List<PreventItemUsePower> preventItemUsePowers = PowerHolderComponent.getPowers(player, PreventItemUsePower.class)
            .stream()
            .filter(p -> p.doesPrevent((ItemStack) (Object) this))
            .toList();

        if (preventItemUsePowers.isEmpty()) {
            return;
        }

        String translationKey = "tooltip.apoli.unusable." + this.getUseAction().toString().toLowerCase(Locale.ROOT) + (preventItemUsePowers.size() == 1 ? ".single" : ".multiple");

        Formatting baseTextFormat = Formatting.GRAY;
        Formatting powerTextFormat = Formatting.RED;

        Text powerText;
        Text baseText;

        if (preventItemUsePowers.size() == 1) {

            PreventItemUsePower preventItemUsePower = preventItemUsePowers.getFirst();

            powerText = preventItemUsePower.getType().getName().formatted(powerTextFormat);
            baseText = Text.translatable(translationKey, powerText).formatted(baseTextFormat);

            apoli$tooltip.add(baseText);

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
                this.apoli$appendExpandedTooltip(preventItemUsePowers, apoli$tooltip, translationKey, powerTextFormat, powerTextFormat);
            }

            else {

                powerText = Text.translatable("tooltip.apoli.usability_hint.power_count", preventItemUsePowers.size()).formatted(powerTextFormat);
                baseText = Text.translatable(translationKey, powerText).formatted(baseTextFormat);

                apoli$tooltip.add(baseText);
                apoli$tooltip.add(Text.empty());

                Text keyBindingText = KeyBindingUtil.getLocalizedName(keyBinding.getTranslationKey()).styled(style -> style
                    .withColor(Formatting.YELLOW)
                    .withItalic(keyBinding.isUnbound()));

                Text guideText = Text.translatable("tooltip.apoli.usability_hint.show_powers", keyBindingText).formatted(baseTextFormat);
                apoli$tooltip.add(guideText);

            }

        }

        else {
            this.apoli$appendExpandedTooltip(preventItemUsePowers, apoli$tooltip, translationKey, powerTextFormat, baseTextFormat);
        }

    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;appendTooltip(Lnet/minecraft/component/ComponentType;Lnet/minecraft/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/item/tooltip/TooltipType;)V"))
    private void apoli$appendPowerTooltips(ItemStack stack, ComponentType<?> componentType, Item.TooltipContext context, Consumer<Text> tooltipConsumer, TooltipType type, Operation<Void> original, Item.TooltipContext mContext, @Nullable PlayerEntity player, @Local List<Text> tooltip) {

        original.call(stack, componentType, context, tooltipConsumer, type);

        if (componentType == DataComponentTypes.LORE) {
            PowerHolderComponent.getPowers(player, TooltipPower.class)
                .stream()
                .filter(p -> p.doesApply((ItemStack) (Object) this))
                .sorted(Comparator.comparing(TooltipPower::getOrder))
                .forEach(p -> p.addToTooltip(tooltipConsumer));
        }

    }

    @Inject(method = "appendAttributeModifiersTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V", shift = At.Shift.AFTER))
    private void apoli$appendStackPowersTooltipWithoutModifiers(Consumer<Text> tooltipConsumer, @Nullable PlayerEntity player, CallbackInfo ci, @Local AttributeModifierSlot modifierSlot, @Local MutableBoolean shouldAppendSlotName) {

        if (apoli$tooltipContext == null || apoli$tooltipType == null || apoli$appendedSlots.contains(modifierSlot)) {
            return;
        }

        StackPowersComponent stackPowers = this.get(ApoliDataComponentTypes.STACK_POWERS_COMPONENT);
        if (stackPowers == null || !stackPowers.containsSlot(modifierSlot)) {
            return;
        }

        tooltipConsumer.accept(ScreenTexts.EMPTY);
        tooltipConsumer.accept(Text.translatable("item.modifiers." + modifierSlot.asString()).formatted(Formatting.GRAY));

        stackPowers.appendTooltip(modifierSlot, apoli$tooltipContext, tooltipConsumer, apoli$tooltipType);
        apoli$appendedSlots.add(modifierSlot);

    }

    @Inject(method = "method_57370", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;appendAttributeModifierTooltip(Ljava/util/function/Consumer;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/entity/attribute/EntityAttributeModifier;)V", shift = At.Shift.AFTER))
    private void apoli$appendStackPowersTooltipWithModifiers(MutableBoolean shouldAppendSlotName, Consumer<Text> tooltipConsumer, AttributeModifierSlot modifierSlot, PlayerEntity playerEntity, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, CallbackInfo ci) {

        if (apoli$tooltip == null || apoli$tooltipContext == null || apoli$tooltipType == null || modifier.value() == 0) {
            return;
        }

        StackPowersComponent stackPowers = this.get(ApoliDataComponentTypes.STACK_POWERS_COMPONENT);
        apoli$appendedSlots.add(modifierSlot);

        if (stackPowers != null && stackPowers.containsSlot(modifierSlot)) {
            stackPowers.appendTooltip(modifierSlot, apoli$tooltipContext, apoli$tooltip::add, apoli$tooltipType);
        }

    }

    @Unique
    private void apoli$appendExpandedTooltip(List<PreventItemUsePower> powers, List<Text> tooltip, String translationKey, Formatting powerTextColor, Formatting baseTextColor) {

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
