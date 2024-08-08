package io.github.apace100.apoli.power;

import com.mojang.serialization.DataResult;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 *  Use {@link PowerTypeManager} instead.
 */
@Deprecated(forRemoval = true)
public class PowerTypeRegistry {

    public static PowerType register(Identifier id, PowerType powerType) {
        return powerType;
    }

    protected static PowerType update(Identifier id, PowerType powerType) {
        return powerType;
    }

    protected static void disable(Identifier id) {
        PowerTypeManager.disable(id);
    }

    protected static void remove(Identifier id) {

    }

    public static boolean isDisabled(Identifier id) {
        return PowerTypeManager.isDisabled(id);
    }

    public static int size() {
        return PowerTypeManager.size();
    }

    public static Stream<Identifier> identifiers() {
        return PowerTypeManager.streamIds();
    }

    public static Iterable<Map.Entry<Identifier, PowerType>> entries() {
        return PowerTypeManager.entrySet();
    }

    public static void forEach(BiConsumer<Identifier, PowerType> powerTypeBiConsumer) {
        PowerTypeManager.forEach(powerTypeBiConsumer);
    }

    public static Iterable<PowerType> values() {
        return PowerTypeManager.values();
    }

    public static DataResult<PowerType> getResult(Identifier id) {
        return PowerTypeManager.getResult(id);
    }

    @Nullable
    public static PowerType getNullable(Identifier id) {
        return PowerTypeManager.getOptional(id).orElse(null);
    }

    public static PowerType get(Identifier id) {
        return PowerTypeManager.get(id);
    }

    public static Identifier getId(PowerType powerType) {
        return powerType.getId();
    }

    public static boolean contains(Identifier id) {
        return PowerTypeManager.contains(id);
    }

    public static void clear() {

    }

    public static void clearDisabledPowers() {
    }

    public static void reset() {

    }

}
