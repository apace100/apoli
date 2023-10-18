package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.ScreenHandlerUsabilityOverride;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.*;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public class CraftingTableAction {

    private static final Text TITLE = Text.translatable("container.crafting");

    public static void action(SerializableData.Instance data, Entity entity) {

        if (!(entity instanceof PlayerEntity playerEntity)) {
            return;
        }

        NamedScreenHandlerFactory namedScreenHandlerFactory = new SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> {

            CraftingScreenHandler craftingScreenHandler = new CraftingScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos()));
            ((ScreenHandlerUsabilityOverride) craftingScreenHandler).apoli$canUse(true);

            return craftingScreenHandler;

        }, TITLE);

        playerEntity.openHandledScreen(namedScreenHandlerFactory);
        playerEntity.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("crafting_table"),
            new SerializableData(),
            CraftingTableAction::action
        );
    }

}
