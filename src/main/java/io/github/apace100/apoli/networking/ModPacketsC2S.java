package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.packet.VersionHandshakePacket;
import io.github.apace100.apoli.networking.packet.c2s.PlayerLandedC2SPacket;
import io.github.apace100.apoli.networking.packet.c2s.PreventedEntityInteractionC2SPacket;
import io.github.apace100.apoli.networking.packet.c2s.UseActivePowersC2SPacket;
import io.github.apace100.apoli.networking.task.VersionHandshakeTask;
import io.github.apace100.apoli.power.*;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@SuppressWarnings("UnstableApiUsage")
public class ModPacketsC2S {

    public static void register() {

        if (Apoli.PERFORM_VERSION_CHECK) {
            ServerConfigurationConnectionEvents.CONFIGURE.register(ModPacketsC2S::handshake);
            ServerConfigurationNetworking.registerGlobalReceiver(VersionHandshakePacket.TYPE, ModPacketsC2S::handleHandshakeReply);
        }

        ServerPlayNetworking.registerGlobalReceiver(UseActivePowersC2SPacket.TYPE, ModPacketsC2S::onUseActivePowers);
        ServerPlayNetworking.registerGlobalReceiver(PlayerLandedC2SPacket.TYPE, ModPacketsC2S::onPlayerLanded);
        ServerPlayNetworking.registerGlobalReceiver(PreventedEntityInteractionC2SPacket.TYPE, ModPacketsC2S::onPreventedEntityInteraction);

    }

    private static void handshake(ServerConfigurationNetworkHandler handler, MinecraftServer server) {

        if (ServerConfigurationNetworking.canSend(handler, VersionHandshakePacket.TYPE)) {
            handler.addTask(new VersionHandshakeTask(Apoli.SEMVER));
            return;
        }

        handler.disconnect(Text.of("This server requires you to install the Apoli mod (v" + Apoli.VERSION + ") to play."));

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

    private static void onUseActivePowers(UseActivePowersC2SPacket packet, ServerPlayerEntity player, PacketSender responseSender) {

        PowerHolderComponent component = PowerHolderComponent.KEY.get(player);
        for (Identifier powerTypeId : packet.powerTypeIds()) {

            PowerType<?> powerType = PowerTypeRegistry.getNullable(powerTypeId);
            if (powerType == null) {
                Apoli.LOGGER.warn("Found unknown power \"{}\" while receiving packet for triggering active powers of player {}!", powerTypeId, player.getName().getString());
                continue;
            }

            Power power = component.getPower(powerType);
            if (power instanceof Active activePower) {
                activePower.onUse();
            }

        }

    }

    private static void onPlayerLanded(PlayerLandedC2SPacket packet, ServerPlayerEntity player, PacketSender responseSender) {
        PowerHolderComponent.getPowers(player, ActionOnLandPower.class).forEach(ActionOnLandPower::executeAction);
    }

    private static void onPreventedEntityInteraction(PreventedEntityInteractionC2SPacket packet, ServerPlayerEntity player, PacketSender responseSender) {}

}
