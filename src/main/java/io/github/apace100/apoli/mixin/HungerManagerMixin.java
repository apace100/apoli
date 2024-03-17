package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.access.PotentiallyEdibleItemStack;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(HungerManager.class)
public class HungerManagerMixin {

    @Shadow
    private int foodLevel;

    @Shadow
    private float saturationLevel;

    @Unique
    private PlayerEntity apoli$cachedPlayer;

    @Unique
    private boolean apoli$ShouldUpdateManually = false;

    @ModifyExpressionValue(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;isFood()Z"))
    private boolean apoli$allowConsumingStack(boolean original, Item item, ItemStack stack) {
        return original || ((PotentiallyEdibleItemStack) stack).apoli$getFoodComponent().isPresent();
    }

    @ModifyExpressionValue(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getFoodComponent()Lnet/minecraft/item/FoodComponent;"))
    private FoodComponent apoli$getOrReplaceFoodComponent(FoodComponent original, Item item, ItemStack stack) {
        return ((PotentiallyEdibleItemStack) stack)
            .apoli$getFoodComponent()
            .orElse(original);
    }

    @ModifyExpressionValue(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/FoodComponent;getHunger()I"))
    private int apoli$modifyHunger(int original, Item item, ItemStack stack) {

        apoli$ShouldUpdateManually = false;
        if (apoli$cachedPlayer == null) {
            return original;
        }

        List<Modifier> modifiers = ((ModifiableFoodEntity) apoli$cachedPlayer).apoli$getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack))
            .flatMap(p -> p.getFoodModifiers().stream())
            .toList();

        int newFood = (int) ModifierUtil.applyModifiers(apoli$cachedPlayer, modifiers, original);
        if (newFood != original && newFood == 0) {
            apoli$ShouldUpdateManually = true;
        }

        return newFood;

    }

    @ModifyExpressionValue(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/FoodComponent;getSaturationModifier()F"))
    private float apoli$modifySaturation(float original, Item item, ItemStack stack) {

        if (apoli$cachedPlayer == null) {
            return original;
        }

        List<Modifier> modifiers = ((ModifiableFoodEntity) apoli$cachedPlayer).apoli$getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack))
            .flatMap(p -> p.getSaturationModifiers().stream())
            .toList();

        float newSaturation = (float) ModifierUtil.applyModifiers(apoli$cachedPlayer, modifiers, original);
        if (newSaturation != original && newSaturation == 0) {
            apoli$ShouldUpdateManually = true;
        }

        return newSaturation;

    }

    @Inject(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;add(IF)V", shift = At.Shift.AFTER))
    private void apoli$executeAdditionalEatAction(Item item, ItemStack stack, CallbackInfo ci) {

        if (apoli$cachedPlayer == null || apoli$cachedPlayer.getWorld().isClient) {
            return;
        }

        ((ModifiableFoodEntity) apoli$cachedPlayer).apoli$getCurrentModifyFoodPowers()
            .stream()
            .filter(p -> p.doesApply(stack))
            .forEach(ModifyFoodPower::eat);

        if (apoli$ShouldUpdateManually) {
            ((ServerPlayerEntity) apoli$cachedPlayer).networkHandler.sendPacket(new HealthUpdateS2CPacket(apoli$cachedPlayer.getHealth(), foodLevel, saturationLevel));
        }

    }

    @Inject(method = "update", at = @At("HEAD"))
    private void apoli$cachePlayer(PlayerEntity player, CallbackInfo ci) {
        this.apoli$cachedPlayer = player;
    }

}
