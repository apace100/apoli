package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.mixin.ClientPlayerEntityAccessor;
import io.github.apace100.apoli.mixin.ClientPlayerInteractionManagerAccessor;
import io.github.apace100.apoli.mixin.ServerPlayerInteractionManagerAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class UsingEffectiveToolEntityConditionType extends EntityConditionType {

    @Override
    public boolean test(Entity entity) {

        if (!(entity instanceof PlayerEntity playerEntity)) {
            return false;
        }

        BlockState miningBlockState;
        if (playerEntity instanceof ServerPlayerEntity serverPlayer) {

            ServerPlayerInteractionManagerAccessor interactionManager = (ServerPlayerInteractionManagerAccessor) serverPlayer.interactionManager;
            if (!interactionManager.getMining()) {
                return false;
            }

            miningBlockState = entity.getWorld().getBlockState(interactionManager.getMiningPos());

        }

        else if (playerEntity instanceof ClientPlayerEntity clientPlayer) {

            ClientPlayerInteractionManagerAccessor interactionManager = (ClientPlayerInteractionManagerAccessor) ((ClientPlayerEntityAccessor) clientPlayer).getClient().interactionManager;
            if (interactionManager == null || !interactionManager.getBreakingBlock()) {
                return false;
            }

            miningBlockState = entity.getWorld().getBlockState(interactionManager.getCurrentBreakingPos());

        }

        else {
            return false;
        }

        return playerEntity.canHarvest(miningBlockState);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.USING_EFFECTIVE_TOOL;
    }

}
