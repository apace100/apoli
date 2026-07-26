package io.github.apace100.apoli.component;

import com.google.common.collect.Lists;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.integration.ModifyValueCallback;
import io.github.apace100.apoli.networking.packet.s2c.SyncBulkPowerDataS2CPacket;
import io.github.apace100.apoli.networking.packet.s2c.SyncPowerDataS2CPacket;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.power.type.ValueModifyingPowerType;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.sync.ComponentPacketWriter;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

//  TODO: Maybe use data attachments instead? -eggohito
public interface PowerHolderComponent extends AutoSyncedComponent, CommonTickingComponent {

    /**
     *  <b>Use {@link #getOptional(Entity)} or {@link #getNullable(Entity)} wherever possible.</b>
     */
    @ApiStatus.Internal
    ComponentKey<PowerHolderComponent> KEY = ComponentRegistry.getOrCreate(Apoli.identifier("powers"), PowerHolderComponent.class);

    boolean removePower(Power power, Identifier source);

    default boolean removePower(PowerReference powerReference, Identifier source) {
        return powerReference.getResultPower()
            .mapError(err -> "Couldn't revoke non-existing power with ID \"" + powerReference.id() + "\"!")
            .resultOrPartial(Apoli.LOGGER::warn)
            .map(power -> removePower(power, source))
            .orElse(false);
    }

    int removeAllPowersFromSource(Identifier source);

    List<Power> getPowersFromSource(Identifier source);

    boolean addPower(Power power, Identifier source);

    default boolean addPower(PowerReference powerReference, Identifier source) {
        return powerReference.getResultPower()
            .mapError(error -> "Couldn't grant non-existing power with ID \"" + powerReference.id() + "\"!")
            .resultOrPartial(Apoli.LOGGER::warn)
            .map(power -> addPower(power, source))
            .orElse(false);
    }

    boolean hasPower(Power power);

    default boolean hasPower(PowerReference powerReference) {
        return powerReference.getOptionalPower()
            .map(this::hasPower)
            .orElse(false);
    }

    boolean hasPower(Power power, Identifier source);

    default boolean hasPower(PowerReference powerReference, Identifier source) {
        return powerReference.getOptionalPower()
            .map(power -> hasPower(power, source))
            .orElse(false);
    }

    PowerType getPowerType(Power power);

    List<PowerType> getPowerTypes();

    Set<Power> getPowers(boolean includeSubPowers);

    <T extends PowerType> List<T> getPowerTypes(Class<T> typeClass);

    <T extends PowerType> List<T> getPowerTypes(Class<T> typeClass, boolean includeInactive);

    List<Identifier> getSources(Power power);

    default List<Identifier> getSources(PowerReference powerReference) {
        return powerReference.getOptionalPower()
            .map(this::getSources)
            .orElseGet(ArrayList::new);
    }

    void sync();

    /**
     *  Queries the {@link PowerHolderComponent} from an {@link Entity}. This is safer and preferred than directly using
     *  {@link #KEY} as it handles certain scenarios where unexpected errors may occur.
     *
     *  @param entity   the entity to get the component from
     *  @return         the power component, or {@link Optional#empty()} if the entity is either null, its component
     *                  container hasn't been initialized yet, or if the entity doesn't/can't have the power component
     */
	static Optional<PowerHolderComponent> getOptional(@Nullable Entity entity) {
        return Optional.ofNullable(getNullable(entity));
    }

    /**
     *  An alternative version of {@link #getOptional(Entity)} that returns a <b>nullable</b>
     *  {@link PowerHolderComponent} instead.
     *
     *  @param entity   the entity to get the power component from
     *  @return         the power component, or {@code null} if the entity is either null, its component container
     *                  hasn't been initialized yet, or if the entity doesn't/can't have the power component
     */
    @Nullable
    static PowerHolderComponent getNullable(@Nullable Entity entity) {

	    //noinspection ConstantValue
	    if (entity != null && entity.asComponentProvider().getComponentContainer() != null) {
            return KEY.getNullable(entity);
        }

        else {
            return null;
        }

    }

    static void sync(Entity entity) {
        getOptional(entity).ifPresent(PowerHolderComponent::sync);
    }

    static boolean grantPower(@NotNull Entity entity, Power power, Identifier source, boolean sync) {
        return grantPowers(entity, Map.of(source, List.of(power)), sync);
    }

    static boolean grantPowers(@NotNull Entity entity, Map<Identifier, Collection<Power>> powersBySource, boolean sync) {

        PowerHolderComponent powerComponent = getNullable(entity);
        boolean granted = false;

        if (!entity.getWorld().isClient() && powerComponent != null) {

            for (var entry : powersBySource.entrySet()) {

                for (var power : entry.getValue()) {
                    granted |= powerComponent.addPower(power, entry.getKey());
                }

            }

            if (sync && granted) {
                PacketHandlers.GRANT_POWERS.sync(entity, powersBySource);
            }

        }

        return granted;

    }

    static boolean revokePower(@NotNull Entity entity, Power power, boolean sync) {

        PowerHolderComponent powerComponent = getNullable(entity);
        Map<Identifier, Collection<Power>> revokedPowers = new Object2ObjectLinkedOpenHashMap<>();

        if (!entity.getWorld().isClient() && powerComponent != null) {

            for (var source : powerComponent.getSources(power)) {

                if (powerComponent.removePower(power, source)) {
                    revokedPowers.putIfAbsent(source, Set.of(power));
                }

            }

            if (sync && !revokedPowers.isEmpty()) {
                PacketHandlers.REVOKE_POWERS.sync(entity, revokedPowers);
            }

        }

        return !revokedPowers.isEmpty();

    }

    static boolean revokePower(@NotNull Entity entity, Power power, Identifier source, boolean sync) {
        return revokePowers(entity, Map.of(source, List.of(power)), sync);
    }

    static boolean revokePowers(@NotNull Entity entity, Map<Identifier, Collection<Power>> powersBySource, boolean sync) {

        PowerHolderComponent powerComponent = getNullable(entity);
        Map<Identifier, Collection<Power>> revokedPowers = new Object2ObjectLinkedOpenHashMap<>();

        if (!entity.getWorld().isClient() && powerComponent != null) {

            for (var idAndPowers : powersBySource.entrySet()) {

                Identifier id = idAndPowers.getKey();
                Collection<Power> powers = idAndPowers.getValue();

                for (var power : powers) {

                    if (powerComponent.removePower(power, id)) {
                        revokedPowers.computeIfAbsent(id, k -> new ObjectArrayList<>()).add(power);
                    }

                }

            }

            if (sync && !revokedPowers.isEmpty()) {
                PacketHandlers.REVOKE_POWERS.sync(entity, revokedPowers);
            }

        }

        return !revokedPowers.isEmpty();

    }

    static int revokeAllPowersFromSource(@NotNull Entity entity, Identifier source, boolean sync) {
        return revokeAllPowersFromAllSources(entity, List.of(source), sync);
    }

    static int revokeAllPowersFromAllSources(@NotNull Entity entity, Collection<Identifier> sources, boolean sync) {

        PowerHolderComponent powerComponent = getNullable(entity);
        int revokedPowers = 0;

        if (!entity.getWorld().isClient() && powerComponent != null) {

            for (var source : sources) {
                revokedPowers += powerComponent.removeAllPowersFromSource(source);
            }

            if (sync && revokedPowers > 0) {
                PacketHandlers.REVOKE_ALL_POWERS.sync(entity, sources);
            }

        }

        return revokedPowers;

    }

    static void syncPower(Entity entity, PowerReference powerReference) {
        syncPower(entity, powerReference.getNullablePower());
    }

    static void syncPower(@Nullable Entity entity, @Nullable Power power) {

        if (power == null || entity == null || entity.getWorld().isClient()) {
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

        for (var tracker : MiscUtil.getTrackingSafely(entity)) {
            ServerPlayNetworking.send(tracker, syncPowerDataPacket);
        }

    }

    static <P extends Power> void syncPowers(@Nullable Entity entity, Collection<P> powers) {

        if (entity == null || entity.getWorld().isClient() || powers.isEmpty()) {
            return;
        }

        PowerHolderComponent component = getNullable(entity);
        Map<Identifier, NbtElement> powersToSync = new HashMap<>();

        if (component == null) {
            return;
        }

        for (Power power : powers) {

            PowerType powerType = component.getPowerType(power);

            if (powerType != null) {
                powersToSync.put(power.getId(), powerType.toTag());
            }

        }

        if (powersToSync.isEmpty()) {
            return;
        }

        SyncBulkPowerDataS2CPacket syncBulkPowerDataPacket = new SyncBulkPowerDataS2CPacket(entity.getId(), powersToSync);

        for (var tracker : MiscUtil.getTrackingSafely(entity)) {
            ServerPlayNetworking.send(tracker, syncBulkPowerDataPacket);
        }

    }

    static <T extends PowerType> boolean withPowerType(@Nullable Entity entity, Class<T> powerClass, @NotNull Predicate<T> filter, Consumer<T> action) {

        List<T> types = getPowerTypes(entity, powerClass);
        boolean found = false;

        for (var type : types) {

            if (filter.test(type)) {

                action.accept(type);
                found = true;

                break;

            }

        }

        return found;

    }

    static <T extends PowerType> boolean withPowerTypes(@Nullable Entity entity, Class<T> powerClass, @NotNull Predicate<T> filter, @NotNull Consumer<T> action) {

        List<T> types = getPowerTypes(entity, powerClass);
        boolean found = false;

        for (var type : types) {

            if (filter.test(type)) {
                action.accept(type);
                found = true;
            }

        }

        return found;

    }

    static <T extends PowerType> List<T> getPowerTypes(Entity entity, Class<T> powerClass) {
        return getPowerTypes(entity, powerClass, false);
    }

    static <T extends PowerType> List<T> getPowerTypes(Entity entity, Class<T> powerClass, boolean includeInactive) {
        return getOptional(entity)
            .map(powerComponent -> powerComponent.getPowerTypes(powerClass, includeInactive))
            .orElse(Lists.newArrayList());
    }

    static <T extends PowerType> boolean hasPowerType(Entity entity, Class<T> powerClass) {
        return hasPowerType(entity, powerClass, p -> true);
    }

	static <T extends PowerType> boolean hasPowerType(Entity entity, Class<T> typeClass, @NotNull Predicate<T> typeFilter) {
        return hasPowerType(entity, typeClass, typeFilter, false);
    }

    static <T extends PowerType> boolean hasPowerType(Entity entity, Class<T> typeClass, @NotNull Predicate<T> typeFilter, boolean includeInactive) {

        for (var type : getPowerTypes(entity, typeClass, includeInactive)) {

            if (typeFilter.test(type)) {
                return true;
            }

        }

        return false;

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
        return modify(entity, powerClass, baseValue, powerFilter, powerAction, false);
    }

    static <T extends ValueModifyingPowerType> double modify(Entity entity, Class<T> powerClass, double baseValue, @NotNull Predicate<T> powerFilter, @NotNull Consumer<T> powerAction, boolean includeInactive) {
        return modify(entity, powerClass, ValueModifyingPowerType::getModifiers, baseValue, powerFilter, powerAction, includeInactive);
    }

    static <T extends ValueModifyingPowerType> double modify(Entity entity, Class<T> powerClass, Function<T, List<Modifier>> modifiersGetter, double baseValue, @NotNull Predicate<T> filter, @NotNull Consumer<T> action, boolean includeInactive) {
        return modify(entity, powerClass, getPowerTypes(entity, powerClass, includeInactive), modifiersGetter, baseValue, filter, action);
    }

    static <T extends ValueModifyingPowerType> double modify(Entity entity, Class<T> powerClass, List<T> powerTypes, Function<T, List<Modifier>> modifiersGetter, double baseValue, @NotNull Predicate<T> filter, @NotNull Consumer<T> action) {

        PowerHolderComponent powerComponent = getNullable(entity);
        if (powerComponent != null) {

            List<Modifier> modifiers = new ObjectArrayList<>();
            for (var powerType : powerTypes) {

                if (!filter.test(powerType)) {
                    continue;
                }

                action.accept(powerType);
                modifiers.addAll(modifiersGetter.apply(powerType));

            }

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

            if (getNullable(powerHolder) != null) {

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
