package io.github.apace100.apoli.global;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GlobalPowerSetUtil {

    public static final Identifier POWER_SOURCE = Apoli.identifier("global");

    public static List<GlobalPowerSet> getApplicableSets(EntityType<?> type) {
        return GlobalPowerSetManager.values()
            .stream()
            .filter(gps -> gps.doesApply(type))
            .sorted(GlobalPowerSet::compareTo)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public static Set<Identifier> getPowerIds(List<GlobalPowerSet> powerSets) {
        return powerSets.stream()
            .flatMap(gps -> gps.getPowers().stream())
            .map(Power::getId)
            .collect(Collectors.toSet());
    }

    public static Set<Power> flattenPowers(Collection<GlobalPowerSet> sets) {
        return sets
            .stream()
            .map(GlobalPowerSet::getPowers)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    public static void applyGlobalPowers(Entity entity) {

        if (entity.getWorld().isClient || !PowerHolderComponent.KEY.isProvidedBy(entity)) {
            return;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);

        List<GlobalPowerSet> globalPowerSets = getApplicableSets(entity.getType());
        Set<Power> powers = flattenPowers(globalPowerSets);

        Set<Power> removedPowers = removeExcessPowers(component, powers);
        Set<Power> addedPowers = addMissingPowers(component, powers);

        if (!removedPowers.isEmpty()) {
            PowerHolderComponent.PacketHandlers.REVOKE_POWERS.sync(entity, Map.of(POWER_SOURCE, removedPowers));
        }

        if (!addedPowers.isEmpty()) {
            PowerHolderComponent.PacketHandlers.GRANT_POWERS.sync(entity, Map.of(POWER_SOURCE, addedPowers));
        }

    }

    private static Set<Power> removeExcessPowers(PowerHolderComponent component, Set<Power> expected) {
        return component.getPowersFromSource(POWER_SOURCE)
            .stream()
            .filter(Predicate.not(expected::contains))
            .filter(power -> component.removePower(power, POWER_SOURCE))
            .collect(Collectors.toSet());
    }

    private static Set<Power> addMissingPowers(PowerHolderComponent component, Set<Power> powers) {
        return powers
            .stream()
            .filter(power -> component.addPower(power, POWER_SOURCE))
            .collect(Collectors.toSet());
    }

}
