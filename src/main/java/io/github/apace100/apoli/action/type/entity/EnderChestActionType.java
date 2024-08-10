package io.github.apace100.apoli.action.type.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public class EnderChestActionType {

    public static void action(Entity entity) {

        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        EnderChestInventory enderChestContainer = player.getEnderChestInventory();
        ScreenHandlerFactory handlerFactory = (syncId, playerInventory, _player) -> GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, enderChestContainer);

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(handlerFactory, Text.translatable("container.enderchest")));
        player.incrementStat(Stats.OPEN_ENDERCHEST);

    }

}

