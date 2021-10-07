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
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientLoginNetworking.registerGlobalReceiver(ModPackets.HANDSHAKE, ModPacketsS2C::handleHandshake);
        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            ClientPlayNetworking.registerReceiver(ModPackets.POWER_LIST, ModPacketsS2C::receivePowerList);
            ClientPlayNetworking.registerReceiver(ModPackets.PLAYER_MOUNT, ModPacketsS2C::onPlayerMount);
            ClientPlayNetworking.registerReceiver(ModPackets.PLAYER_DISMOUNT, ModPacketsS2C::onPlayerDismount);
            ClientPlayNetworking.registerReceiver(ModPackets.SET_ATTACKER, ModPacketsS2C::onSetAttacker);
        }));
    }

    private static void onSetAttacker(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        int targetId = packetByteBuf.readInt();
        boolean hasAttacker = packetByteBuf.readBoolean();
        int attackerId = 0;
        if(hasAttacker) {
            attackerId = packetByteBuf.readInt();
        }
        int finalAttackerId = attackerId;
        minecraftClient.execute(() -> {
            Entity target = clientPlayNetworkHandler.getWorld().getEntityById(targetId);
            Entity attacker = null;
            if(hasAttacker) {
                attacker = clientPlayNetworkHandler.getWorld().getEntityById(finalAttackerId);
            }
            if (!(target instanceof LivingEntity)) {
                Apoli.LOGGER.warn("Received unknown target");
            } else if(hasAttacker && !(attacker instanceof LivingEntity)) {
                Apoli.LOGGER.warn("Received unknown attacker");
            } else {
                if(hasAttacker) {
                    ((LivingEntity)target).setAttacker((LivingEntity)attacker);
                } else {
                    ((LivingEntity)target).setAttacker(null);
                }
            }
        });
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

    @Environment(EnvType.CLIENT)
    private static void onPlayerMount(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        int mountingPlayerId = packetByteBuf.readInt();
        int mountedPlayerId = packetByteBuf.readInt();
        minecraftClient.execute(() -> {
            Entity mountingPlayer = clientPlayNetworkHandler.getWorld().getEntityById(mountingPlayerId);
            Entity mountedPlayer = clientPlayNetworkHandler.getWorld().getEntityById(mountedPlayerId);
            if (mountedPlayer == null) {
                Apoli.LOGGER.warn("Received passenger for unknown player");
            } else if(mountingPlayer == null) {
                Apoli.LOGGER.warn("Received unknown passenger for player");
            } else {
                boolean result = mountingPlayer.startRiding(mountedPlayer, true);
                if(result) {
                    Apoli.LOGGER.info(mountingPlayer.getDisplayName().asString() + " started riding " + mountedPlayer.getDisplayName().asString());
                } else {
                    Apoli.LOGGER.warn(mountingPlayer.getDisplayName().asString() + " failed to start riding " + mountedPlayer.getDisplayName().asString());
                }
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private static void onPlayerDismount(MinecraftClient minecraftClient, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        int dismountingPlayerId = packetByteBuf.readInt();
        minecraftClient.execute(() -> {
            Entity dismountingPlayer = clientPlayNetworkHandler.getWorld().getEntityById(dismountingPlayerId);
            if (dismountingPlayer == null) {
                Apoli.LOGGER.warn("Unknown player tried to dismount");
            } else {
                if(dismountingPlayer.getVehicle() instanceof PlayerEntity) {
                    dismountingPlayer.dismountVehicle();
                }
            }
        });
    }
}
