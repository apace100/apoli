package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.networking.packet.s2c.MountPlayerS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public class MountBiEntityActionType extends BiEntityActionType {

    @Override
	protected void execute(Entity actor, Entity target) {

        if (actor == null || target == null) {
            return;
        }

        actor.startRiding(target, true);
        if (target instanceof ServerPlayerEntity targetPlayer) {
            ServerPlayNetworking.send(targetPlayer, new MountPlayerS2CPacket(actor.getId(), target.getId()));
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BiEntityActionTypes.MOUNT;
    }

}
