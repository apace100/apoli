package io.github.apace100.apoli.component;

import com.google.common.collect.Lists;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.integration.ModifyValueCallback;
import io.github.apace100.apoli.networking.packet.s2c.SyncPowerS2CPacket;
import io.github.apace100.apoli.networking.packet.s2c.SyncPowersInBulkS2CPacket;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface PowerHolderComponent extends AutoSyncedComponent, ServerTickingComponent {

    ComponentKey<PowerHolderComponent> KEY = ComponentRegistry.getOrCreate(Apoli.identifier("powers"), PowerHolderComponent.class);

    void removePower(PowerType<?> powerType, Identifier source);

    int removeAllPowersFromSource(Identifier source);

    List<PowerType<?>> getPowersFromSource(Identifier source);

    boolean addPower(PowerType<?> powerType, Identifier source);

    boolean hasPower(PowerType<?> powerType);

    boolean hasPower(PowerType<?> powerType, Identifier source);

    <T extends Power> T getPower(PowerType<T> powerType);

    List<Power> getPowers();

    Set<PowerType<?>> getPowerTypes(boolean getSubPowerTypes);

    <T extends Power> List<T> getPowers(Class<T> powerClass);

    <T extends Power> List<T> getPowers(Class<T> powerClass, boolean includeInactive);

    List<Identifier> getSources(PowerType<?> powerType);

    void sync();

    static void sync(Entity entity) {
        KEY.sync(entity);
    }

    static void syncPower(Entity entity, PowerType<?> powerType) {

        if (entity == null || entity.getWorld().isClient) {
            return;
        }

        if (powerType instanceof PowerTypeReference<?> powerTypeRef) {
            powerType = powerTypeRef.getReferencedPowerType();
        }

        if (powerType == null) {
            return;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(entity);
        if (component == null) {
            return;
        }

        NbtCompound powerData = new NbtCompound();
        Power power = component.getPower(powerType);

        if (power == null) {
            return;
        }

        powerData.put("Data", power.toTag(true));
        SyncPowerS2CPacket syncPowerPacket = new SyncPowerS2CPacket(entity.getId(), powerType.getIdentifier(), powerData);

        for (ServerPlayerEntity otherPlayer : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(otherPlayer, syncPowerPacket);
        }

        if (entity instanceof ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, syncPowerPacket);
        }

    }

    static void syncPowers(Entity entity, Collection<? extends PowerType<?>> powerTypes) {

        if (entity == null || entity.getWorld().isClient) {
            return;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(entity);
        Map<Identifier, NbtElement> powersToSync = new HashMap<>();

        if (component == null) {
            return;
        }

        for (PowerType<?> powerType : powerTypes) {

            if (powerType instanceof PowerTypeReference<?> powerTypeRef) {
                powerType = powerTypeRef.getReferencedPowerType();
            }

            if (powerType == null) {
                continue;
            }

            Power power = component.getPower(powerType);
            if (power != null) {
                powersToSync.put(powerType.getIdentifier(), power.toTag(true));
            }

        }

        if (powersToSync.isEmpty()) {
            return;
        }

        SyncPowersInBulkS2CPacket syncPowersPacket = new SyncPowersInBulkS2CPacket(entity.getId(), powersToSync);
        for (ServerPlayerEntity otherPlayer : PlayerLookup.tracking(entity)) {

            if (ServerPlayNetworking.canSend(otherPlayer, SyncPowersInBulkS2CPacket.TYPE)) {
                ServerPlayNetworking.send(otherPlayer, syncPowersPacket);
            }

        }

        if (entity instanceof ServerPlayerEntity player && ServerPlayNetworking.canSend(player, SyncPowersInBulkS2CPacket.TYPE)) {
            ServerPlayNetworking.send(player, syncPowersPacket);
        }

    }

    static <T extends Power> void withPower(Entity entity, Class<T> powerClass, Predicate<T> power, Consumer<T> with) {
        if(entity instanceof LivingEntity) {
            Optional<T> optional = KEY.get(entity).getPowers(powerClass).stream().filter(p -> power == null || power.test(p)).findAny();
            optional.ifPresent(with);
        }
    }

    static <T extends Power> void withPowers(Entity entity, Class<T> powerClass, Predicate<T> filter, Consumer<T> with) {
        if(entity instanceof LivingEntity) {
            KEY.get(entity).getPowers(powerClass).stream().filter(p -> filter == null || filter.test(p)).forEach(with);
        }
    }

    static <T extends Power> List<T> getPowers(Entity entity, Class<T> powerClass) {
        return getPowers(entity, powerClass, false);
    }

    static <T extends Power> List<T> getPowers(Entity entity, Class<T> powerClass, boolean includeInactive) {
        return KEY.maybeGet(entity)
            .map(pc -> pc.getPowers(powerClass, includeInactive))
            .orElse(Lists.newArrayList());
    }

    static <T extends Power> boolean hasPower(Entity entity, Class<T> powerClass) {
        return hasPower(entity, powerClass, null);
    }

    static <T extends Power> boolean hasPower(Entity entity, Class<T> powerClass, Predicate<T> powerFilter) {
        if(entity instanceof LivingEntity) {
            return KEY.get(entity).getPowers().stream()
                .anyMatch(p -> powerClass.isAssignableFrom(p.getClass()) && p.isActive() &&
                    (powerFilter == null || powerFilter.test((T)p)));
        }
        return false;
    }

    static <T extends ValueModifyingPower> float modify(Entity entity, Class<T> powerClass, float baseValue) {
        return (float)modify(entity, powerClass, (double)baseValue, null, null);
    }

    static <T extends ValueModifyingPower> float modify(Entity entity, Class<T> powerClass, float baseValue, Predicate<T> powerFilter) {
        return (float)modify(entity, powerClass, (double)baseValue, powerFilter, null);
    }

    static <T extends ValueModifyingPower> float modify(Entity entity, Class<T> powerClass, float baseValue, Predicate<T> powerFilter, Consumer<T> powerAction) {
        return (float)modify(entity, powerClass, (double)baseValue, powerFilter, powerAction);
    }

    static <T extends ValueModifyingPower> double modify(Entity entity, Class<T> powerClass, double baseValue) {
        return modify(entity, powerClass, baseValue, null, null);
    }

    static <T extends ValueModifyingPower> double modify(Entity entity, Class<T> powerClass, double baseValue, Predicate<T> powerFilter, Consumer<T> powerAction) {
        if(entity instanceof LivingEntity living) {
            PowerHolderComponent powerHolder = PowerHolderComponent.KEY.get(entity);
            List<T> powers = powerHolder.getPowers(powerClass);
            List<Modifier> mps = powers.stream()
                .filter(p -> powerFilter == null || powerFilter.test(p))
                .flatMap(p -> p.getModifiers().stream()).collect(Collectors.toList());
            if(powerAction != null) {
                powers.stream().filter(p -> powerFilter == null || powerFilter.test(p)).forEach(powerAction);
            }

            powerHolder.getPowers(AttributeModifyTransferPower.class).stream()
                .filter(p -> p.doesApply(powerClass)).forEach(p -> p.addModifiers(mps));
            ModifyValueCallback.EVENT.invoker().collectModifiers(living, powerClass, baseValue, mps);
            return ModifierUtil.applyModifiers(entity, mps, baseValue);
        }
        return baseValue;
    }
}
