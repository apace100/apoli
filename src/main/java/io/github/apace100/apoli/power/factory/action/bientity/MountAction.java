package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.networking.packet.s2c.MountPlayerS2CPacket;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;

public class MountAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        Entity actor = actorAndTarget.getLeft();
        Entity target = actorAndTarget.getRight();

        if (actor == null || target == null) {
            return;
        }

        actor.startRiding(target, true);
        if (!actor.getWorld().isClient && target instanceof ServerPlayerEntity targetPlayer) {
            ServerPlayNetworking.send(targetPlayer, new MountPlayerS2CPacket(actor.getId(), target.getId()));
        }

    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("mount"),
            new SerializableData(),
            MountAction::action
        );
    }

}
