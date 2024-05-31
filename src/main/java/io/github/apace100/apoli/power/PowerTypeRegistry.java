package io.github.apace100.apoli.power;

import io.github.apace100.apoli.integration.PowerClearCallback;
import io.github.apace100.apoli.integration.PowerOverrideCallback;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class PowerTypeRegistry {

    private static final HashMap<Identifier, PowerType> idToPower = new HashMap<>();
    private static final Set<Identifier> disabledPowers = new HashSet<>();

    public static PowerType register(Identifier id, PowerType powerType) {
        if(idToPower.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate power type id tried to register: '" + id.toString() + "'");
        }
        disabledPowers.remove(id);
        idToPower.put(id, powerType);
        return powerType;
    }

    protected static PowerType update(Identifier id, PowerType powerType) {

        PowerType oldPower = idToPower.remove(id);
        if (oldPower instanceof MultiplePowerType<?> oldMultiple) {
            oldMultiple.getSubPowers().forEach(PowerTypeRegistry::remove);
        }

        PowerOverrideCallback.EVENT.invoker().onPowerOverride(id);
        return register(id, powerType);

    }

    protected static void disable(Identifier id) {
        remove(id);
        disabledPowers.add(id);
    }

    protected static void remove(Identifier id) {
        idToPower.remove(id);
    }

    public static boolean isDisabled(Identifier id) {
        return disabledPowers.contains(id);
    }

    public static int size() {
        return idToPower.size();
    }

    public static Stream<Identifier> identifiers() {
        return idToPower.keySet().stream();
    }

    public static Iterable<Map.Entry<Identifier, PowerType>> entries() {
        return idToPower.entrySet();
    }

    public static void forEach(BiConsumer<Identifier, PowerType<?>> powerTypeBiConsumer) {
        idToPower.forEach(powerTypeBiConsumer::accept);
    }

    public static Iterable<PowerType> values() {
        return idToPower.values();
    }

    @Nullable
    public static PowerType getNullable(Identifier id) {
        return idToPower.get(id);
    }

    public static PowerType get(Identifier id) {
        if(!idToPower.containsKey(id)) {
            throw new IllegalArgumentException("Could not get power type from id '" + id.toString() + "', as it was not registered!");
        }
        PowerType powerType = idToPower.get(id);
        return powerType;
    }

    public static Identifier getId(PowerType<?> powerType) {
        return powerType.getIdentifier();
    }

    public static boolean contains(Identifier id) {
        return idToPower.containsKey(id);
    }

    public static void clear() {
        PowerClearCallback.EVENT.invoker().onPowerClear();
        idToPower.clear();
    }

    public static void clearDisabledPowers() {
        disabledPowers.clear();
    }

    public static void reset() {
        clear();
        clearDisabledPowers();
    }
}
