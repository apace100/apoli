package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.packet.VersionHandshakePacket;
import io.github.apace100.apoli.networking.task.VersionHandshakeTask;
import io.github.apace100.apoli.power.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class ModPacketsC2S {

    public static void register() {

        if (Apoli.PERFORM_VERSION_CHECK) {
            ServerConfigurationConnectionEvents.CONFIGURE.register(ModPacketsC2S::handshake);
            ServerConfigurationNetworking.registerGlobalReceiver(VersionHandshakePacket.TYPE, ModPacketsC2S::handleHandshakeReply);
        }

        ServerPlayNetworking.registerGlobalReceiver(ModPackets.USE_ACTIVE_POWERS, ModPacketsC2S::useActivePowers);
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.PLAYER_LANDED, ModPacketsC2S::playerLanded);
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.PREVENTED_ENTITY_USE, ModPacketsC2S::interactionPrevented);

    }

    private static void playerLanded(MinecraftServer minecraftServer, ServerPlayerEntity playerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        minecraftServer.execute(() -> PowerHolderComponent.getPowers(playerEntity, ActionOnLandPower.class).forEach(ActionOnLandPower::executeAction));
    }

    private static void interactionPrevented(MinecraftServer minecraftServer, ServerPlayerEntity playerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        int otherEntityId = packetByteBuf.readInt();
        int handOrdinal = packetByteBuf.readInt();
        minecraftServer.execute(() -> {
            Entity otherEntity = playerEntity.getWorld().getEntityById(otherEntityId);
            Hand hand = Hand.values()[handOrdinal];
            if(otherEntity == null) {
                Apoli.LOGGER.warn("Received unknown entity for prevented interaction");
            } else {
                boolean prevented = false;
                for(PreventEntityUsePower peup : PowerHolderComponent.getPowers(playerEntity, PreventEntityUsePower.class)) {
                    if(peup.doesApply(otherEntity, hand, playerEntity.getStackInHand(hand))) {
                        peup.executeAction(otherEntity, hand);
                        prevented = true;
                        break;
                    }
                }
                if(!prevented) {
                    for(PreventBeingUsedPower pbup : PowerHolderComponent.getPowers(otherEntity, PreventBeingUsedPower.class)) {
                        if(pbup.doesApply(playerEntity, hand, playerEntity.getStackInHand(hand))) {
                            pbup.executeAction(playerEntity, hand);
                            prevented = true;
                            break;
                        }
                    }
                    if(!prevented) {
                        Apoli.LOGGER.warn("Couldn't find corresponding entity use preventing power");
                    }
                }
            }
        });
    }

    private static void useActivePowers(MinecraftServer minecraftServer, ServerPlayerEntity playerEntity, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        int count = packetByteBuf.readInt();
        Identifier[] powerIds = new Identifier[count];
        for(int i = 0; i < count; i++) {
            powerIds[i] = packetByteBuf.readIdentifier();
        }
        minecraftServer.execute(() -> {
            PowerHolderComponent component = PowerHolderComponent.KEY.get(playerEntity);
            for(Identifier id : powerIds) {
                PowerType<?> type = PowerTypeRegistry.get(id);
                Power power = component.getPower(type);
                if(power instanceof Active) {
                    ((Active)power).onUse();
                }
            }
        });
    }

    private static void handleHandshakeReply(VersionHandshakePacket packet, ServerConfigurationNetworkHandler handler, PacketSender responseSender) {

        boolean mismatch = packet.semver().length != Apoli.SEMVER.length;
        for (int i = 0; !mismatch && i < packet.semver().length - 1; i++) {

            if (packet.semver()[i] != Apoli.SEMVER[i]) {
                mismatch = true;
                break;
            }

        }

        if (!mismatch) {
            handler.completeTask(VersionHandshakeTask.KEY);
            return;
        }

        StringBuilder semverString = new StringBuilder();
        String separator = "";

        for (int i : packet.semver()) {
            semverString.append(separator).append(i);
            separator = ".";
        }

        handler.disconnect(Text.translatable("apoli.gui.version_mismatch", Apoli.VERSION, semverString));

    }

    private static void handshake(ServerConfigurationNetworkHandler handler, MinecraftServer server) {

        if (ServerConfigurationNetworking.canSend(handler, VersionHandshakePacket.TYPE)) {
            handler.addTask(new VersionHandshakeTask(Apoli.SEMVER));
        } else {
            handler.disconnect(Text.of("This server requires you to install the Apoli mod (v" + Apoli.VERSION + ") to play."));
        }

    }

}
