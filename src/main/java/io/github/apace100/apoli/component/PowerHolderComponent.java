package io.github.apace100.apoli.component;

import com.google.common.collect.Lists;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.integration.ModifyValueCallback;
import io.github.apace100.apoli.networking.packet.s2c.SyncPowerS2CPacket;
import io.github.apace100.apoli.networking.packet.s2c.SyncPowersInBulkS2CPacket;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.AttributeModifyTransferPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.power.type.ValueModifyingPowerType;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface PowerHolderComponent extends AutoSyncedComponent, ServerTickingComponent {

    ComponentKey<PowerHolderComponent> KEY = ComponentRegistry.getOrCreate(Apoli.identifier("powers"), PowerHolderComponent.class);

    boolean removePower(Power power, Identifier source);

    int removeAllPowersFromSource(Identifier source);

    List<Power> getPowersFromSource(Identifier source);

    boolean addPower(Power power, Identifier source);

    boolean hasPower(Power power);

    boolean hasPower(Power power, Identifier source);

    PowerType getPowerType(Power power);

    List<PowerType> getPowerTypes();

    Set<Power> getPowers(boolean includeSubPowers);

    <T extends PowerType> List<T> getPowerTypes(Class<T> powerClass);

    <T extends PowerType> List<T> getPowerTypes(Class<T> powerClass, boolean includeInactive);

    List<Identifier> getSources(Power power);

    void sync();

    static void sync(Entity entity) {
        KEY.sync(entity);
    }

    static void syncPower(Entity entity, Power power) {

        if (entity == null || entity.getWorld().isClient) {
            return;
        }

        if (power instanceof PowerReference powerTypeRef) {
            power = powerTypeRef.getReference();
        }

        if (power == null) {
            return;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(entity);
        if (component == null) {
            return;
        }

        NbtCompound powerData = new NbtCompound();
        PowerType powerType = component.getPowerType(power);

        if (powerType == null) {
            return;
        }

        powerData.put("Data", powerType.toTag(true));
        SyncPowerS2CPacket syncPowerPacket = new SyncPowerS2CPacket(entity.getId(), power.getId(), powerData);

        for (ServerPlayerEntity otherPlayer : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(otherPlayer, syncPowerPacket);
        }

        if (entity instanceof ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, syncPowerPacket);
        }

    }

    static void syncPowers(Entity entity, Collection<? extends Power> powers) {

        if (entity == null || entity.getWorld().isClient || powers.isEmpty()) {
            return;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(entity);
        Map<Identifier, NbtElement> powersToSync = new HashMap<>();

        if (component == null) {
            return;
        }

        for (Power power : powers) {

            if (power instanceof PowerReference powerTypeRef) {
                power = powerTypeRef.getReference();
            }

            if (power == null) {
                continue;
            }

            PowerType powerType = component.getPowerType(power);
            if (powerType != null) {
                powersToSync.put(power.getId(), powerType.toTag(true));
            }

        }

        if (powersToSync.isEmpty()) {
            return;
        }

        SyncPowersInBulkS2CPacket syncPowersPacket = new SyncPowersInBulkS2CPacket(entity.getId(), powersToSync);
        for (ServerPlayerEntity otherPlayer : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(otherPlayer, syncPowersPacket);
        }

        if (entity instanceof ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, syncPowersPacket);
        }

    }

    static <T extends PowerType> boolean withPowerType(@Nullable Entity entity, Class<T> powerClass, @NotNull Predicate<T> filter, Consumer<T> action) {

        Optional<T> power = KEY.maybeGet(entity)
            .stream()
            .flatMap(pc -> pc.getPowerTypes(powerClass).stream())
            .filter(filter)
            .findFirst();

        power.ifPresent(action);
        return power.isPresent();

    }

    static <T extends PowerType> boolean withPowerTypes(@Nullable Entity entity, Class<T> powerClass, @NotNull Predicate<T> filter, @NotNull Consumer<T> action) {

        List<T> powerTypes = KEY.maybeGet(entity)
            .stream()
            .flatMap(pc -> pc.getPowerTypes(powerClass).stream())
            .filter(filter)
            .toList();

        powerTypes.forEach(action);
        return !powerTypes.isEmpty();

    }

    static <T extends PowerType> List<T> getPowerTypes(Entity entity, Class<T> powerClass) {
        return getPowerTypes(entity, powerClass, false);
    }

    static <T extends PowerType> List<T> getPowerTypes(Entity entity, Class<T> powerClass, boolean includeInactive) {
        return KEY.maybeGet(entity)
            .map(pc -> pc.getPowerTypes(powerClass, includeInactive))
            .orElse(Lists.newArrayList());
    }

    static <T extends PowerType> boolean hasPowerType(Entity entity, Class<T> powerClass) {
        return hasPowerType(entity, powerClass, p -> true);
    }

    static <T extends PowerType> boolean hasPowerType(Entity entity, Class<T> powerClass, @NotNull Predicate<T> powerFilter) {
        return KEY.maybeGet(entity)
            .stream()
            .flatMap(pc -> pc.getPowerTypes().stream())
            .filter(p -> powerClass.isAssignableFrom(p.getClass()))
            .anyMatch(p -> p.isActive() && powerFilter.test(powerClass.cast(p)));
    }

    static <T extends ValueModifyingPowerType> float modify(Entity entity, Class<T> powerClass, float baseValue) {
        return (float) modify(entity, powerClass, (double) baseValue, p -> true, p -> {});
    }

    static <T extends ValueModifyingPowerType> float modify(Entity entity, Class<T> powerClass, float baseValue, Predicate<T> powerFilter) {
        return (float) modify(entity, powerClass, (double) baseValue, powerFilter, p -> {});
    }

    static <T extends ValueModifyingPowerType> float modify(Entity entity, Class<T> powerClass, float baseValue, Predicate<T> powerFilter, Consumer<T> powerAction) {
        return (float) modify(entity, powerClass, (double) baseValue, powerFilter, powerAction);
    }

    static <T extends ValueModifyingPowerType> double modify(Entity entity, Class<T> powerClass, double baseValue) {
        return modify(entity, powerClass, baseValue, p -> true, p -> {});
    }

    static <T extends ValueModifyingPowerType> double modify(Entity entity, Class<T> powerClass, double baseValue, @NotNull Predicate<T> powerFilter, @NotNull Consumer<T> powerAction) {

        if (entity != null && KEY.isProvidedBy(entity)) {

            PowerHolderComponent component = KEY.get(entity);
            List<Modifier> modifiers = component.getPowerTypes(powerClass)
                .stream()
                .filter(powerFilter)
                .peek(powerAction)
                .flatMap(p -> p.getModifiers().stream())
                .collect(Collectors.toCollection(ArrayList::new));

            component.getPowerTypes(AttributeModifyTransferPowerType.class)
                .stream()
                .filter(p -> p.doesApply(powerClass))
                .forEach(p -> p.addModifiers(modifiers));

            ModifyValueCallback.EVENT.invoker().collectModifiers(entity, powerClass, baseValue, modifiers);
            return ModifierUtil.applyModifiers(entity, modifiers, baseValue);

        }

        else {
            return baseValue;
        }

    }

}
