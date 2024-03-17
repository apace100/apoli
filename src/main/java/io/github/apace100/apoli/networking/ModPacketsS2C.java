package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.packet.VersionHandshakePacket;
import io.github.apace100.apoli.networking.packet.s2c.*;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.OptionalInt;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
@Environment(EnvType.CLIENT)
public class ModPacketsS2C {

    public static void register() {

        ClientConfigurationNetworking.registerGlobalReceiver(VersionHandshakePacket.TYPE, ModPacketsS2C::handleHandshake);

        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            ClientPlayNetworking.registerReceiver(SyncPowerTypeRegistryS2CPacket.TYPE, ModPacketsS2C::onPowerTypeRegistrySync);
            ClientPlayNetworking.registerReceiver(SyncPowerS2CPacket.TYPE, ModPacketsS2C::onPowerSync);
            ClientPlayNetworking.registerReceiver(MountPlayerS2CPacket.TYPE, ModPacketsS2C::onPlayerMount);
            ClientPlayNetworking.registerReceiver(DismountPlayerS2CPacket.TYPE, ModPacketsS2C::onPlayerDismount);
            ClientPlayNetworking.registerReceiver(SyncAttackerS2CPacket.TYPE, ModPacketsS2C::onAttackerSync);
            ClientPlayNetworking.registerReceiver(SyncStatusEffectS2CPacket.TYPE, ModPacketsS2C::onStatusEffectSync);
        }));

    }

    private static void handleHandshake(VersionHandshakePacket packet, PacketSender responseSender) {
        responseSender.sendPacket(new VersionHandshakePacket(Apoli.SEMVER));
    }

    private static void onStatusEffectSync(SyncStatusEffectS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {

        Entity target = player.networkHandler.getWorld().getEntityById(packet.targetId());
        if (!(target instanceof LivingEntity livingTarget)) {
            Apoli.LOGGER.warn("Received packet for syncing status effect of {} entity!", (target == null ? "an unknown" : "a non-living"));
            return;
        }

        StatusEffectInstance statusEffectInstance = packet.updateType() != SyncStatusEffectsUtil.UpdateType.CLEAR ? StatusEffectInstance.fromNbt(packet.statusEffectData()) : null;
        packet.updateType().accept(livingTarget, statusEffectInstance);

    }

    private static void onAttackerSync(SyncAttackerS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {

        Entity target = player.networkHandler.getWorld().getEntityById(packet.targetId());
        if (!(target instanceof LivingEntity livingTarget)) {
            Apoli.LOGGER.warn("Received packet for syncing the attacker of {} entity!", (target == null ? "an unknown" : "a non-living"));
            return;
        }

        OptionalInt attackerId = packet.attackerId();
        if (attackerId.isEmpty()) {
            livingTarget.setAttacker(null);
            return;
        }

        Entity attacker = player.networkHandler.getWorld().getEntityById(attackerId.getAsInt());
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            Apoli.LOGGER.warn("Received packet for syncing non-living attacker of entity \"{}\"!", target.getName().getString());
            return;
        }

        livingTarget.setAttacker(livingAttacker);

    }

    private static void onPowerTypeRegistrySync(SyncPowerTypeRegistryS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {
        PowerTypeRegistry.clear();
        packet.powers().forEach(PowerTypeRegistry::register);
    }

    private static void onPlayerMount(MountPlayerS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {

        Entity actor = player.networkHandler.getWorld().getEntityById(packet.actorId());
        Entity target = player.networkHandler.getWorld().getEntityById(packet.targetId());

        if (target == null) {
            Apoli.LOGGER.warn("Received packet for passenger for unknown player!");
            return;
        }

        if (actor == null) {
            Apoli.LOGGER.warn("Received packet for unknown passenger for player {}!", target.getName().getString());
            return;
        }

        boolean result = actor.startRiding(target, true);

        Consumer<String> loggerMethod = result ? Apoli.LOGGER::info : Apoli.LOGGER::warn;
        String action = result ? " started riding " : " failed to start riding ";

        loggerMethod.accept(actor.getName().getString() + action + target.getName().getString());

    }

    private static void onPlayerDismount(DismountPlayerS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {

        Entity dismountingEntity = player.networkHandler.getWorld().getEntityById(packet.id());
        if (dismountingEntity == null) {
            Apoli.LOGGER.warn("Received packet for unknown entity that tried to dismount!");
            return;
        }

        if (dismountingEntity.getVehicle() instanceof PlayerEntity) {
            dismountingEntity.dismountVehicle();
        }

    }

    private static void onPowerSync(SyncPowerS2CPacket packet, ClientPlayerEntity player, PacketSender responseSender) {

        Identifier powerTypeId = packet.powerTypeId();
        if (!PowerTypeRegistry.contains(powerTypeId)) {
            Apoli.LOGGER.warn("Received packet for syncing unknown power \"{}\"!", powerTypeId);
            return;
        }

        Entity entity = player.networkHandler.getWorld().getEntityById(packet.entityId());
        if (entity == null) {
            Apoli.LOGGER.warn("Received packet for syncing power \"{}\" to unknown entity!", powerTypeId);
            return;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY
            .maybeGet(entity)
            .orElse(null);

        if (component == null) {
            Apoli.LOGGER.warn("Received packet for syncing power \"{}\" to entity \"{}\", which cannot hold powers!", powerTypeId, entity.getName().getString());
            return;
        }

        PowerType<?> powerType = PowerTypeRegistry.get(powerTypeId);
        Power power = component.getPower(powerType);

        if (power != null) {
            power.fromTag(packet.powerData().get("Data"));
        }

    }

}
