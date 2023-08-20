package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HungerManager.class)
public class HungerManagerMixin {

    @Shadow private int foodLevel;
    @Shadow private float saturationLevel;
    @Unique
    private PlayerEntity player;

    @Unique
    private boolean apoli$ShouldUpdateManually = false;

    @Redirect(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/FoodComponent;getHunger()I"))
    private int modifyHunger(FoodComponent foodComponent, Item item, ItemStack stack) {

        apoli$ShouldUpdateManually = false;
        int baseValue = foodComponent.getHunger();

        if (player == null) return baseValue;

        List<Modifier> modifiers = ((ModifiableFoodEntity) player).apoli$getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack))
            .flatMap(p -> p.getFoodModifiers().stream())
            .toList();

        int newFood = (int) ModifierUtil.applyModifiers(player, modifiers, baseValue);
        if (newFood != baseValue && newFood == 0) apoli$ShouldUpdateManually = true;

        return newFood;

    }

    @Redirect(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/FoodComponent;getSaturationModifier()F"))
    private float modifySaturation(FoodComponent foodComponent, Item item, ItemStack stack) {

        float baseValue = foodComponent.getSaturationModifier();
        if (player == null) return baseValue;

        List<Modifier> modifiers = ((ModifiableFoodEntity) player).apoli$getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack))
            .flatMap(p -> p.getSaturationModifiers().stream())
            .toList();

        float newSaturation = (float) ModifierUtil.applyModifiers(player, modifiers, baseValue);
        if (newSaturation != baseValue && newSaturation == 0) apoli$ShouldUpdateManually = true;

        return newSaturation;

    }

    @Inject(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;add(IF)V", shift = At.Shift.AFTER))
    private void executeAdditionalEatAction(Item item, ItemStack stack, CallbackInfo ci) {

        if (player == null || player.getWorld().isClient) return;

        ((ModifiableFoodEntity) player).apoli$getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack))
            .forEach(ModifyFoodPower::eat);

        if (apoli$ShouldUpdateManually) ((ServerPlayerEntity) player).networkHandler.sendPacket(new HealthUpdateS2CPacket(player.getHealth(), foodLevel, saturationLevel));

    }

    @Inject(method = "update", at = @At("HEAD"))
    private void cachePlayer(PlayerEntity player, CallbackInfo ci) {
        this.player = player;
    }
}
