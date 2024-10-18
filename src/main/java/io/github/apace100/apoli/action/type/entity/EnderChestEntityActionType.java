package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public class EnderChestEntityActionType extends EntityActionType {

    @Override
    protected void execute(Entity entity) {

        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        EnderChestInventory enderChestContainer = player.getEnderChestInventory();
        ScreenHandlerFactory handlerFactory = (syncId, playerInventory, _player) -> GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, enderChestContainer);

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(handlerFactory, Text.translatable("container.enderchest")));
        player.incrementStat(Stats.OPEN_ENDERCHEST);

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.ENDER_CHEST;
    }

}
