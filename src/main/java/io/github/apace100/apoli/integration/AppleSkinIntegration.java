package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.access.PotentiallyEdibleItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;

import java.util.List;

public class AppleSkinIntegration implements AppleSkinApi {

    @Override
    public void registerEvents() {

        FoodValuesEvent.EVENT.register(event -> {

            PlayerEntity player = event.player;
            ItemStack stack = event.itemStack;

            ((PotentiallyEdibleItemStack) stack)
                .apoli$getFoodComponent()
                .ifPresent(fc -> event.modifiedFoodValues = new FoodValues(fc.getHunger(), fc.getSaturationModifier()));

            List<ModifyFoodPower> modifyFoodPowers = PowerHolderComponent.getPowers(player, ModifyFoodPower.class)
                .stream()
                .filter(p -> p.doesApply(stack))
                .toList();
            if (modifyFoodPowers.isEmpty()) {
                return;
            }

            FoodValues originalValues = !event.modifiedFoodValues.equals(event.defaultFoodValues)
                ? event.modifiedFoodValues
                : event.defaultFoodValues;

            List<Modifier> foodModifiers = modifyFoodPowers
                .stream()
                .flatMap(p -> p.getFoodModifiers().stream())
                .toList();
            List<Modifier> saturationModifiers = modifyFoodPowers
                .stream()
                .flatMap(p -> p.getFoodModifiers().stream())
                .toList();

            int hunger = (int) ModifierUtil.applyModifiers(player, foodModifiers, originalValues.hunger);
            float saturation = (float) ModifierUtil.applyModifiers(player, saturationModifiers, originalValues.saturationModifier);

            event.modifiedFoodValues = new FoodValues(hunger, saturation);

        });

    }

}
