package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.CustomToastViewer;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.packet.VersionHandshakePacket;
import io.github.apace100.apoli.networking.packet.s2c.*;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.SyncStatusEffectsUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class ModPacketsS2C {

    public static void register() {

        ClientConfigurationNetworking.registerGlobalReceiver(VersionHandshakePacket.PACKET_ID, ModPacketsS2C::sendHandshakeReply);

        ClientPlayConnectionEvents.INIT.register(((handler, client) -> {
            ClientPlayNetworking.registerReceiver(SyncPowerTypesS2CPacket.PACKET_ID, PowerManager::receive);
            ClientPlayNetworking.registerReceiver(SyncPowerS2CPacket.PACKET_ID, ModPacketsS2C::onPowerSync);
            ClientPlayNetworking.registerReceiver(SyncPowersInBulkS2CPacket.PACKET_ID, ModPacketsS2C::onPowerSyncInBulk);
            ClientPlayNetworking.registerReceiver(MountPlayerS2CPacket.PACKET_ID, ModPacketsS2C::onPlayerMount);
            ClientPlayNetworking.registerReceiver(DismountPlayerS2CPacket.PACKET_ID, ModPacketsS2C::onPlayerDismount);
            ClientPlayNetworking.registerReceiver(SyncAttackerS2CPacket.PACKET_ID, ModPacketsS2C::onAttackerSync);
            ClientPlayNetworking.registerReceiver(SyncStatusEffectS2CPacket.PACKET_ID, ModPacketsS2C::onStatusEffectSync);
            ClientPlayNetworking.registerReceiver(ShowToastS2CPacket.PACKET_ID, ModPacketsS2C::onShowToast);
        }));

    }

    private static void sendHandshakeReply(VersionHandshakePacket packet, ClientConfigurationNetworking.Context context) {
        context.responseSender().sendPacket(new VersionHandshakePacket(Apoli.SEMVER));
    }

    private static void onStatusEffectSync(SyncStatusEffectS2CPacket payload, ClientPlayNetworking.Context context) {

        ClientPlayerEntity player = context.player();

        Entity target = player.networkHandler.getWorld().getEntityById(payload.targetId());
        SyncStatusEffectsUtil.UpdateType updateType = payload.updateType();

        if (target instanceof LivingEntity livingTarget) {

            StatusEffectInstance statusEffectInstance = updateType != SyncStatusEffectsUtil.UpdateType.CLEAR
                ? StatusEffectInstance.fromNbt(payload.statusEffectData())
                : null;

            updateType.accept(livingTarget, statusEffectInstance);

        }

        else {
            Apoli.LOGGER.warn("Received packet for syncing status effect of {} entity!", (target == null ? "an unknown" : "a non-living"));
        }

    }

    private static void onAttackerSync(SyncAttackerS2CPacket payload, ClientPlayNetworking.Context context) {

        Entity target = context.player().networkHandler.getWorld().getEntityById(payload.targetId());
        if (!(target instanceof LivingEntity livingTarget)) {
            Apoli.LOGGER.warn("Received packet for syncing the attacker of {} entity!", (target == null ? "an unknown" : "a non-living"));
            return;
        }

        Optional<Integer> attackerId = payload.attackerId();
        if (attackerId.isEmpty()) {
            livingTarget.setAttacker(null);
            return;
        }

        Entity attacker = context.player().networkHandler.getWorld().getEntityById(attackerId.get());
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            Apoli.LOGGER.warn("Received packet for syncing non-living attacker of entity \"{}\"!", target.getName().getString());
            return;
        }

        livingTarget.setAttacker(livingAttacker);

    }

    private static void onPlayerMount(MountPlayerS2CPacket packet, ClientPlayNetworking.Context context) {

        ClientPlayNetworkHandler handler = context.player().networkHandler;

        Entity actor = handler.getWorld().getEntityById(packet.actorId());
        Entity target = handler.getWorld().getEntityById(packet.targetId());

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

    private static void onPlayerDismount(DismountPlayerS2CPacket packet, ClientPlayNetworking.Context context) {

        ClientPlayerEntity player = context.player();
        Entity dismountingEntity = player.networkHandler.getWorld().getEntityById(packet.id());

        if (dismountingEntity == null) {
            Apoli.LOGGER.warn("Received packet for unknown entity that tried to dismount!");
        }

        else if (dismountingEntity.getVehicle() instanceof PlayerEntity) {
            dismountingEntity.dismountVehicle();
        }

    }

    private static void onPowerSync(SyncPowerS2CPacket payload, ClientPlayNetworking.Context context) {

        ClientPlayerEntity player = context.player();
        Identifier powerTypeId = payload.powerTypeId();

        if (!PowerManager.contains(powerTypeId)) {
            Apoli.LOGGER.warn("Received packet for syncing unknown power \"{}\"!", powerTypeId);
            return;
        }

        Entity entity = player.networkHandler.getWorld().getEntityById(payload.entityId());
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

        Power power = PowerManager.get(powerTypeId);
        PowerType powerType = component.getPowerType(power);

        if (powerType != null) {
            powerType.fromTag(payload.powerData().get("Data"), true);
        }

    }

    private static void onPowerSyncInBulk(SyncPowersInBulkS2CPacket payload, ClientPlayNetworking.Context context) {

        Entity entity = context.player().getWorld().getEntityById(payload.entityId());
        Map<Identifier, NbtElement> powerAndData = payload.powerAndData();

        if (entity == null) {
            Apoli.LOGGER.warn("Received packet for syncing {} power(s) to unknown entity!", powerAndData.size());
            return;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(entity);
        if (component == null) {
            Apoli.LOGGER.warn("Received packet for syncing {} power(s) to entity \"{}\", which cannot hold powers!", powerAndData.size(), entity.getName().getString());
            return;
        }

        int invalidPowers = 0;
        for (Map.Entry<Identifier, NbtElement> entry : powerAndData.entrySet()) {

            Identifier powerTypeId = entry.getKey();
            NbtElement powerTypeData = entry.getValue();

            if (!PowerManager.contains(powerTypeId)) {
                ++invalidPowers;
                continue;
            }

            Power power = PowerManager.get(powerTypeId);
            PowerType powerType = component.getPowerType(power);

            if (powerType != null) {
                powerType.fromTag(powerTypeData, true);
            }

        }

        if (invalidPowers > 0) {
            Apoli.LOGGER.warn("Received packet for syncing {} invalid power(s) to entity \"{}\"", invalidPowers, entity.getName().getString());
        }

    }

    public static void onShowToast(ShowToastS2CPacket packet, ClientPlayNetworking.Context context) {
        if (context.player() instanceof CustomToastViewer viewer) {
            viewer.apoli$showToast(packet.toastData());
        }
    }

}
