package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.packet.VersionHandshakePacket;
import io.github.apace100.apoli.networking.packet.c2s.UseActivePowersC2SPacket;
import io.github.apace100.apoli.networking.task.VersionHandshakeTask;
import io.github.apace100.apoli.power.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModPacketsC2S {

    public static void register() {

        if (Apoli.PERFORM_VERSION_CHECK) {
            ServerConfigurationConnectionEvents.CONFIGURE.register(ModPacketsC2S::handshake);
            ServerConfigurationNetworking.registerGlobalReceiver(VersionHandshakePacket.PACKET_ID, ModPacketsC2S::handleHandshakeReply);
        }

        ServerPlayConnectionEvents.INIT.register((handler, server) ->
            ServerPlayNetworking.registerReceiver(handler, UseActivePowersC2SPacket.PACKET_ID, ModPacketsC2S::onUseActivePowers)
        );

    }

    private static void handshake(ServerConfigurationNetworkHandler handler, MinecraftServer server) {

        if (ServerConfigurationNetworking.canSend(handler, VersionHandshakePacket.PACKET_ID)) {
            handler.addTask(new VersionHandshakeTask(Apoli.SEMVER));
        }

        else {
            handler.disconnect(Text.of("This server requires you to install the Apoli mod (v" + Apoli.VERSION + ") to play."));
        }

    }


    private static void handleHandshakeReply(VersionHandshakePacket payload, ServerConfigurationNetworking.Context context) {

        ServerConfigurationNetworkHandler handler = context.networkHandler();

        int[] semver = payload.semver();
        boolean mismatch = semver.length != Apoli.SEMVER.length;

        for (int i = 0; !mismatch && i < semver.length - 1; i++) {

            if (semver[i] != Apoli.SEMVER[i]) {
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

        for (int i : payload.semver()) {
            semverString.append(separator).append(i);
            separator = ".";
        }

        handler.disconnect(Text.stringifiedTranslatable("apoli.gui.version_mismatch", Apoli.VERSION, semverString));

    }


    private static void onUseActivePowers(UseActivePowersC2SPacket payload, ServerPlayNetworking.Context context) {

        ServerPlayerEntity player = context.player();
        PowerHolderComponent component = PowerHolderComponent.KEY.get(player);

        for (Identifier powerTypeId : payload.powerTypeIds()) {

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

}
