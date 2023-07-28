package io.github.apace100.apoli.power;

import io.github.apace100.apoli.integration.PowerClearCallback;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PowerTypeRegistry {

    private static final List<PowerTypeReference> preLoadedPowers = new LinkedList<>();
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
        remove(id);
        return register(id, powerType);
    }

    protected static void preLoad(PowerTypeReference powerTypeRef) {
        preLoadedPowers.add(powerTypeRef);
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

    protected static void validatePreLoadedPowers(Consumer<PowerTypeReference> validator) {
        preLoadedPowers.removeIf(powerTypeRef -> {
            validator.accept(powerTypeRef);
            return true;
        });
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

        if (idToPower.containsKey(id)) {
            return idToPower.get(id);
        }

        return preLoadedPowers.stream()
            .filter(ref -> ref.getIdentifier().equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Could not get power type from id '" + id.toString() + "', as it was not registered!"));

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
