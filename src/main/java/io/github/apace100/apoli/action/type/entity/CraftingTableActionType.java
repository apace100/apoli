package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.access.ScreenHandlerUsabilityOverride;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public class CraftingTableActionType {

    public static void action(Entity entity) {

        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        ScreenHandlerFactory handlerFactory = (syncId, playerInventory, _player) -> {

            CraftingScreenHandler craftingScreenHandler = new CraftingScreenHandler(syncId, playerInventory, ScreenHandlerContext.create(player.getWorld(), player.getBlockPos()));
            ((ScreenHandlerUsabilityOverride) craftingScreenHandler).apoli$canUse(true);

            return craftingScreenHandler;

        };

        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(handlerFactory, Text.translatable("container.crafting")));
        player.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);

    }

}
