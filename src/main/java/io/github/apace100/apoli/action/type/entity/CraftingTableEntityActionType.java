package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.access.ScreenHandlerUsabilityOverride;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public class CraftingTableEntityActionType extends EntityActionType {

    @Override
    protected void execute(Entity entity) {

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

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.CRAFTING_TABLE;
    }

}
