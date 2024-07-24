package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.EdibleItemPower;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;

import java.util.List;

public class AppleSkinIntegration implements AppleSkinApi {

    @Override
    public void registerEvents() {

        FoodValuesEvent.EVENT.register(event -> {

            PlayerEntity player = event.player;
            ItemStack stack = event.itemStack;

            EdibleItemPower.get(stack, player)
                .map(EdibleItemPower::getFoodComponent)
                .ifPresent(foodComponent -> event.modifiedFoodComponent = foodComponent);

            List<ModifyFoodPower> modifyFoodPowers = PowerHolderComponent.getPowers(player, ModifyFoodPower.class)
                .stream()
                .filter(p -> p.doesApply(stack))
                .toList();
            if (modifyFoodPowers.isEmpty()) {
                return;
            }

            FoodComponent originalFoodComponent = !event.modifiedFoodComponent.equals(event.defaultFoodComponent)
                ? event.modifiedFoodComponent
                : event.defaultFoodComponent;

            List<Modifier> nutritionModifiers = modifyFoodPowers
                .stream()
                .flatMap(p -> p.getFoodModifiers().stream())
                .toList();
            List<Modifier> saturationModifiers = modifyFoodPowers
                .stream()
                .flatMap(p -> p.getSaturationModifiers().stream())
                .toList();

            int newNutrition = (int) ModifierUtil.applyModifiers(player, nutritionModifiers, originalFoodComponent.nutrition());
            float newSaturation = (float) ModifierUtil.applyModifiers(player, saturationModifiers, originalFoodComponent.saturation());

            event.modifiedFoodComponent = new FoodComponent(
                newNutrition,
                newSaturation,
                originalFoodComponent.canAlwaysEat(),
                originalFoodComponent.eatSeconds(),
                originalFoodComponent.usingConvertsTo(),
                originalFoodComponent.effects()
            );

        });

    }

}
