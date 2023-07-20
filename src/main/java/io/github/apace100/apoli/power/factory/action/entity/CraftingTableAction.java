package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public class CraftingTableAction {
    private static final Text TITLE = Text.translatable("container.crafting");

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof PlayerEntity player)) return;

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, _player) ->
            new CraftingScreenHandler(syncId, inventory, ScreenHandlerContext.create(_player.getWorld(), _player.getBlockPos())), TITLE));

        player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("crafting_table"),
                new SerializableData(),
                CraftingTableAction::action
        );
    }
}
