package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.networking.packet.s2c.MountPlayerS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public class MountActionType {

    public static void action(Entity actor, Entity target) {

        if (actor == null || target == null) {
            return;
        }

        actor.startRiding(target, true);
        if (!actor.getWorld().isClient && target instanceof ServerPlayerEntity targetPlayer) {
            ServerPlayNetworking.send(targetPlayer, new MountPlayerS2CPacket(actor.getId(), target.getId()));
        }

    }

}
