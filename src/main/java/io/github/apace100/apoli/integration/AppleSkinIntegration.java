package io.github.apace100.apoli.integration;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.EdibleItemPower;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AppleSkinIntegration implements AppleSkinApi {
    @Override
    public void registerEvents() {

        FoodValuesEvent.EVENT.register(event -> {
            PlayerEntity player = event.player;
            ItemStack stack = event.itemStack;


            Optional<EdibleItemPower> edibleItemPower = PowerHolderComponent.getPowers(player, EdibleItemPower.class)
                    .stream()
                    .filter(p -> p.doesApply(stack))
                    .max(Comparator.comparing(EdibleItemPower::getPriority));
            edibleItemPower.ifPresent(ediblePower -> event.modifiedFoodValues = new FoodValues(ediblePower.getFoodComponent().getHunger(), ediblePower.getFoodComponent().getSaturationModifier()));

            if (PowerHolderComponent.hasPower(player, ModifyFoodPower.class)) {
                FoodValues originalValues = !event.modifiedFoodValues.equals(event.defaultFoodValues) ? event.modifiedFoodValues : event.defaultFoodValues;

                List<Modifier> foodModifiers = PowerHolderComponent.getPowers(player, ModifyFoodPower.class)
                        .stream()
                        .filter(p -> p.doesApply(stack))
                        .flatMap(p -> p.getFoodModifiers().stream())
                        .toList();
                List<Modifier> saturationModifiers = PowerHolderComponent.getPowers(player, ModifyFoodPower.class)
                        .stream()
                        .filter(p -> p.doesApply(stack))
                        .flatMap(p -> p.getSaturationModifiers().stream())
                        .toList();

                int hunger = (int) ModifierUtil.applyModifiers(player, foodModifiers, originalValues.hunger);
                float saturation = (float) ModifierUtil.applyModifiers(player, saturationModifiers, originalValues.saturationModifier);

                event.modifiedFoodValues = new FoodValues(hunger, saturation);
            }
        });

    }

}
