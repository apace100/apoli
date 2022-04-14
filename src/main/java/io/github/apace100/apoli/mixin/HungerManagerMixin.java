package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ModifiableFoodEntity;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.AttributeUtil;
import net.minecraft.entity.attribute.EntityAttributeModifier;
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
import java.util.stream.Collectors;

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
        if(player != null) {
            double baseValue = foodComponent.getHunger();
        List<EntityAttributeModifier> modifiers = ((ModifiableFoodEntity)player).getCurrentModifyFoodPowers().stream()
            .filter(p -> p.doesApply(stack))
            .flatMap(p -> p.getFoodModifiers().stream()).collect(Collectors.toList());
            int newFood = (int) AttributeUtil.sortAndApplyModifiers(modifiers, baseValue);
            if(newFood != (int)baseValue && newFood == 0) {
                apoli$ShouldUpdateManually = true;
            }
            return newFood;
        }
        return foodComponent.getHunger();
    }

    @Redirect(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/FoodComponent;getSaturationModifier()F"))
    private float modifySaturation(FoodComponent foodComponent, Item item, ItemStack stack) {
        if(player != null) {
            double baseValue = foodComponent.getSaturationModifier();
            List<EntityAttributeModifier> modifiers = ((ModifiableFoodEntity)player).getCurrentModifyFoodPowers().stream()
                .filter(p -> p.doesApply(stack))
                .flatMap(p -> p.getSaturationModifiers().stream()).collect(Collectors.toList());
            float newSaturation = (float) AttributeUtil.sortAndApplyModifiers(modifiers, baseValue);
            if(newSaturation != baseValue && newSaturation == 0) {
                apoli$ShouldUpdateManually = true;
            }
            return newSaturation;
        }
        return foodComponent.getSaturationModifier();
    }

    @Inject(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;add(IF)V", shift = At.Shift.AFTER))
    private void executeAdditionalEatAction(Item item, ItemStack stack, CallbackInfo ci) {
        if(player != null) {
            ((ModifiableFoodEntity)player).getCurrentModifyFoodPowers().stream().filter(p -> p.doesApply(stack)).forEach(ModifyFoodPower::eat);
            if(apoli$ShouldUpdateManually && !player.world.isClient) {
                ((ServerPlayerEntity)player).networkHandler.sendPacket(new HealthUpdateS2CPacket(player.getHealth(), foodLevel, saturationLevel));
            }
        }
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void cachePlayer(PlayerEntity player, CallbackInfo ci) {
        this.player = player;
    }
}
