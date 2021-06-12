package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientLoginNetworking.registerGlobalReceiver(ModPackets.HANDSHAKE, ModPacketsS2C::handleHandshake);
        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            ClientPlayNetworking.registerReceiver(ModPackets.POWER_LIST, ModPacketsS2C::receivePowerList);
        }));
    }


    @Environment(EnvType.CLIENT)
    private static CompletableFuture<PacketByteBuf> handleHandshake(MinecraftClient minecraftClient, ClientLoginNetworkHandler clientLoginNetworkHandler, PacketByteBuf packetByteBuf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(Apoli.SEMVER.length);
        for(int i = 0; i < Apoli.SEMVER.length; i++) {
            buf.writeInt(Apoli.SEMVER[i]);
        }
        return CompletableFuture.completedFuture(buf);
    }

    @Environment(EnvType.CLIENT)
    private static void receivePowerList(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        int powerCount = packetByteBuf.readInt();
        HashMap<Identifier, PowerType> factories = new HashMap<>();
        for(int i = 0; i < powerCount; i++) {
            Identifier powerId = packetByteBuf.readIdentifier();
            Identifier factoryId = packetByteBuf.readIdentifier();
            try {
                PowerFactory factory = ApoliRegistries.POWER_FACTORY.get(factoryId);
                PowerFactory.Instance factoryInstance = factory.read(packetByteBuf);
                PowerType type;
                if(packetByteBuf.readBoolean()) {
                    type = new MultiplePowerType(powerId, factoryInstance);
                    int subPowerCount = packetByteBuf.readVarInt();
                    List<Identifier> subPowers = new ArrayList<>(subPowerCount);
                    for(int j = 0; j < subPowerCount; j++) {
                        subPowers.add(packetByteBuf.readIdentifier());
                    }
                    ((MultiplePowerType)type).setSubPowers(subPowers);
                } else {
                    type = new PowerType(powerId, factoryInstance);
                }
                type.setTranslationKeys(packetByteBuf.readString(), packetByteBuf.readString());
                if (packetByteBuf.readBoolean()) {
                    type.setHidden();
                }
                factories.put(powerId, type);
            } catch(Exception e) {
                Apoli.LOGGER.error("Error while receiving \"" + powerId + "\" (factory: \"" + factoryId + "\"): " + e.getMessage());
                e.printStackTrace();
            }
        }
        minecraftClient.execute(() -> {
            PowerTypeRegistry.clear();
            factories.forEach(PowerTypeRegistry::register);
        });
    }
}
