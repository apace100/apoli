package io.github.apace100.apoli.power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.integration.PowerClearCallback;
import io.github.apace100.apoli.integration.PowerOverrideCallback;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class PowerTypeRegistry {

    public static final Codec<Identifier> VALIDATING_CODEC = Identifier.CODEC.comapFlatMap(
        id -> contains(id)
            ? DataResult.success(id)
            : DataResult.error(() -> "Couldn't get power type from id '" + id + "', as it was not registered!"),
        Function.identity()
    );
    public static final Codec<PowerType<?>> DISPATCH_CODEC = Identifier.CODEC.comapFlatMap(
        PowerTypeRegistry::getResult,
        PowerType::getIdentifier
    );
    public static final PacketCodec<ByteBuf, PowerType<?>> DISPATCH_PACKET_CODEC = Identifier.PACKET_CODEC.xmap(
        PowerTypeRegistry::get,
        PowerType::getIdentifier
    );

    private static final Map<Identifier, PowerType<?>> ID_TO_POWER = new HashMap<>();
    private static final Set<Identifier> DISABLED = new HashSet<>();

    public static PowerType<?> register(Identifier id, PowerType<?> powerType) {

        if(ID_TO_POWER.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate power type id tried to register: '" + id.toString() + "'");
        }

        DISABLED.remove(id);
        ID_TO_POWER.put(id, powerType);

        return powerType;

    }

    protected static PowerType<?> update(Identifier id, PowerType<?> powerType) {

        PowerType<?> oldPower = ID_TO_POWER.remove(id);
        if (oldPower instanceof MultiplePowerType<?> oldMultiple) {
            oldMultiple.getSubPowers().forEach(PowerTypeRegistry::remove);
        }

        PowerOverrideCallback.EVENT.invoker().onPowerOverride(id);
        return register(id, powerType);

    }

    protected static void disable(Identifier id) {
        remove(id);
        DISABLED.add(id);
    }

    protected static void remove(Identifier id) {
        ID_TO_POWER.remove(id);
    }

    public static boolean isDisabled(Identifier id) {
        return DISABLED.contains(id);
    }

    public static int size() {
        return ID_TO_POWER.size();
    }

    public static Stream<Identifier> identifiers() {
        return ID_TO_POWER.keySet().stream();
    }

    public static Iterable<Map.Entry<Identifier, PowerType<?>>> entries() {
        return ID_TO_POWER.entrySet();
    }

    public static void forEach(BiConsumer<Identifier, PowerType<?>> powerTypeBiConsumer) {
        ID_TO_POWER.forEach(powerTypeBiConsumer);
    }

    public static Iterable<PowerType<?>> values() {
        return ID_TO_POWER.values();
    }

    public static DataResult<PowerType<?>> getResult(Identifier id) {
        return contains(id)
            ? DataResult.success(get(id))
            : DataResult.error(() -> "Could not get power type from id '" + id + "', as it was not registered!");
    }

    @Nullable
    public static PowerType<?> getNullable(Identifier id) {
        return ID_TO_POWER.get(id);
    }

    public static PowerType<?> get(Identifier id) {

        if (!ID_TO_POWER.containsKey(id)) {
            throw new IllegalArgumentException("Could not get power type from id '" + id.toString() + "', as it was not registered!");
        }

        else {
            return ID_TO_POWER.get(id);
        }

    }

    public static Identifier getId(PowerType<?> powerType) {
        return powerType.getIdentifier();
    }

    public static boolean contains(Identifier id) {
        return ID_TO_POWER.containsKey(id);
    }

    public static void clear() {
        PowerClearCallback.EVENT.invoker().onPowerClear();
        ID_TO_POWER.clear();
    }

    public static void clearDisabledPowers() {
        DISABLED.clear();
    }

    public static void reset() {
        clear();
        clearDisabledPowers();
    }
}
