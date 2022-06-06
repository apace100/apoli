package io.github.apace100.apoli.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public class EnderChestAction {
    private static final Text TITLE = Text.translatable("container.enderchest");

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof PlayerEntity)) return;

        PlayerEntity player = (PlayerEntity) entity;
        EnderChestInventory enderChestContainer = player.getEnderChestInventory();

        player.openHandledScreen(
                new SimpleNamedScreenHandlerFactory((i, inventory, _player) ->
                        GenericContainerScreenHandler.createGeneric9x3(i, inventory, enderChestContainer),
                        TITLE
                )
        );

        player.incrementStat(Stats.OPEN_ENDERCHEST);
    }

    public static ActionFactory<Entity> createFactory() {
        return new ActionFactory<>(Apoli.identifier("ender_chest"),
                new SerializableData(),
                EnderChestAction::action
        );
    }
}

