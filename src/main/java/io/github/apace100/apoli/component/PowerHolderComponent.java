package io.github.apace100.apoli.component;

import com.google.common.collect.Lists;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.integration.ModifyValueCallback;
import io.github.apace100.apoli.networking.packet.s2c.SyncBulkPowerDataS2CPacket;
import io.github.apace100.apoli.networking.packet.s2c.SyncPowerDataS2CPacket;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.sync.ComponentPacketWriter;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
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

    <T extends PowerType> List<T> getPowerTypes(Class<T> typeClass);

    <T extends PowerType> List<T> getPowerTypes(Class<T> typeClass, boolean includeInactive);

    List<Identifier> getSources(Power power);

    void sync();

    static void sync(Entity entity) {

        if (KEY.isProvidedBy(entity)) {
            KEY.sync(entity);
        }

    }

    static boolean grantPower(@NotNull Entity entity, Power power, Identifier source, boolean sync) {
        return grantPowers(entity, Map.of(source, List.of(power)), sync);
    }

    static boolean grantPowers(@NotNull Entity entity, Map<Identifier, Collection<Power>> powersBySource, boolean sync) {

        if (KEY.isProvidedBy(entity) && !entity.getWorld().isClient) {

            PowerHolderComponent component = KEY.get(entity);
            boolean granted = powersBySource.entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream().map(power -> component.addPower(power, e.getKey())))
                .reduce(false, Boolean::logicalOr);

            if (granted && sync) {
                PacketHandlers.GRANT_POWERS.sync(entity, powersBySource);
            }

            return granted;

        }

        else {
            return false;
        }

    }

    static boolean revokePower(@NotNull Entity entity, Power power, Identifier source, boolean sync) {
        return revokePowers(entity, Map.of(source, List.of(power)), sync);
    }

    static boolean revokePowers(@NotNull Entity entity, Map<Identifier, Collection<Power>> powersBySource, boolean sync) {

        if (KEY.isProvidedBy(entity) && !entity.getWorld().isClient) {

            PowerHolderComponent component = KEY.get(entity);
            boolean revoked = powersBySource.entrySet()
                .stream()
                .flatMap(e -> e.getValue().stream().map(power -> component.removePower(power, e.getKey())))
                .reduce(false, Boolean::logicalOr);

            if (revoked && sync) {
                PacketHandlers.REVOKE_POWERS.sync(entity, powersBySource);
            }

            return revoked;

        }

        else {
            return false;
        }

    }

    static int revokeAllPowersFromSource(@NotNull Entity entity, Identifier source, boolean sync) {
        return revokeAllPowersFromAllSources(entity, List.of(source), sync);
    }

    static int revokeAllPowersFromAllSources(@NotNull Entity entity, Collection<Identifier> sources, boolean sync) {

        if (KEY.isProvidedBy(entity) && !entity.getWorld().isClient) {

            PowerHolderComponent component = KEY.get(entity);
            int revokedPowers = sources
                .stream()
                .map(component::removeAllPowersFromSource)
                .reduce(0, Integer::sum);

            if (revokedPowers > 0 && sync) {
                PacketHandlers.REVOKE_ALL_POWERS.sync(entity, sources);
            }

            return revokedPowers;

        }

        else {
            return 0;
        }

    }

    static void syncPower(Entity entity, Power power) {

        if (entity == null || entity.getWorld().isClient) {
            return;
        }

        if (power instanceof PowerReference powerReference) {
            power = powerReference.getReference();
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

        powerData.put("Data", powerType.toTag());
        SyncPowerDataS2CPacket syncPowerDataPacket = new SyncPowerDataS2CPacket(entity.getId(), power.getId(), powerData);

        for (ServerPlayerEntity trackingPlayer : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(trackingPlayer, syncPowerDataPacket);
        }

        if (entity instanceof ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, syncPowerDataPacket);
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

            if (power instanceof PowerReference powerReference) {
                power = powerReference.getReference();
            }

            if (power == null) {
                continue;
            }

            PowerType powerType = component.getPowerType(power);
            if (powerType != null) {
                powersToSync.put(power.getId(), powerType.toTag());
            }

        }

        if (powersToSync.isEmpty()) {
            return;
        }

        SyncBulkPowerDataS2CPacket syncBulkPowerDataPacket = new SyncBulkPowerDataS2CPacket(entity.getId(), powersToSync);
        for (ServerPlayerEntity otherPlayer : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(otherPlayer, syncBulkPowerDataPacket);
        }

        if (entity instanceof ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, syncBulkPowerDataPacket);
        }

    }

    static <T extends PowerType> boolean withPowerType(@Nullable Entity entity, Class<T> powerClass, @NotNull Predicate<T> filter, Consumer<T> action) {

        Optional<T> power = KEY.maybeGet(entity)
            .stream()
            .map(pc-> pc.getPowerTypes(powerClass))
            .flatMap(Collection::stream)
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

    static <T extends PowerType> boolean hasPowerType(Entity entity, Class<T> typeClass, @NotNull Predicate<T> typeFilter) {
        return KEY.maybeGet(entity)
            .stream()
            .map(PowerHolderComponent::getPowerTypes)
            .flatMap(Collection::stream)
            .filter(typeClass::isInstance)
            .map(typeClass::cast)
            .anyMatch(type -> type.isActive() && typeFilter.test(type));
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

    final class PacketHandlers {

        public static final PacketHandler<Map<Identifier, Collection<Power>>> GRANT_POWERS = new PacketHandler.Impl<>(
            powersBySource -> (buf, recipient) -> buf.writeMap(powersBySource,
                PacketByteBuf::writeIdentifier,
                (vbuf, powers) -> vbuf.writeCollection(powers, (ebuf, power) -> ebuf.writeIdentifier(power.getId()))
            ),
            (buf, component) -> {

                var powersBySource = buf.readMap(
                    PacketByteBuf::readIdentifier,
                    vbuf -> vbuf.readCollection(ArrayList::new, ebuf -> PowerManager.get(ebuf.readIdentifier())));

                powersBySource.forEach((source, powers) -> powers.forEach(power -> component.addPower(power, source)));

            },
            1
        );

        public static final PacketHandler<Map<Identifier, Collection<Power>>> REVOKE_POWERS = new PacketHandler.Impl<>(
            GRANT_POWERS::write,
            (buf, component) -> {

                var powersBySource = buf.readMap(
                    PacketByteBuf::readIdentifier,
                    vbuf -> vbuf.readCollection(ArrayList::new, ebuf -> PowerManager.get(ebuf.readIdentifier())));

                powersBySource.forEach((source, powers) -> powers.forEach(power -> component.removePower(power, source)));

            },
            2
        );

        public static final PacketHandler<Collection<Identifier>> REVOKE_ALL_POWERS = new PacketHandler.Impl<>(
            sources -> (buf, recipient) ->
                buf.writeCollection(sources, PacketByteBuf::writeIdentifier),
            (buf, component) -> buf
                .readCollection(ArrayList::new, PacketByteBuf::readIdentifier)
                .forEach(component::removeAllPowersFromSource),
            3
        );

    }

    abstract sealed class PacketHandler<T> permits PacketHandler.Impl {

        public abstract ComponentPacketWriter write(T type);

        public abstract void apply(RegistryByteBuf buf, PowerHolderComponent component);

        public abstract int getId();

        public final void sync(Entity powerHolder, T type) {

            if (KEY.isProvidedBy(powerHolder)) {

                KEY.sync(powerHolder, (buf, recipient) -> {

                    buf.writeVarInt(this.getId());
                    this.write(type).writeSyncPacket(buf, recipient);

                });

            }

        }

        public static final class Impl<T> extends PacketHandler<T> {

            final Function<T, ComponentPacketWriter> writer;
            final BiConsumer<RegistryByteBuf, PowerHolderComponent> applier;

            final int id;

            private Impl(Function<T, ComponentPacketWriter> writer, BiConsumer<RegistryByteBuf, PowerHolderComponent> applier, int id) {
                this.writer = writer;
                this.applier = applier;
                this.id = id;
            }

            @Override
            public ComponentPacketWriter write(T type) {
                return writer.apply(type);
            }

            @Override
            public void apply(RegistryByteBuf buf, PowerHolderComponent component) {
                applier.accept(buf, component);
            }

            @Override
            public int getId() {
                return id;
            }

        }

    }

}
