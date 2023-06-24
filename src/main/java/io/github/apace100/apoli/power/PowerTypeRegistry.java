package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.integration.PowerClearCallback;
import net.minecraft.util.Identifier;

import java.util.*;
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
        if(idToPower.containsKey(id)) {
            PowerType old = idToPower.get(id);
            idToPower.remove(id);
        }
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

    public static Iterable<PowerType> values() {
        return idToPower.values();
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
