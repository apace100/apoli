package io.github.apace100.apoli.global;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GlobalPowerSetUtil {

    public static final Identifier POWER_SOURCE = Apoli.identifier("global");

    public static List<GlobalPowerSet> getApplicableSets(EntityType<?> type) {
        return GlobalPowerSetLoader.ALL
            .values()
            .stream()
            .filter(gps -> gps.doesApply(type))
            .sorted(GlobalPowerSet::compareTo)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    public static Set<Identifier> getPowerTypeIds(List<GlobalPowerSet> powerSets) {
        return powerSets.stream()
            .flatMap(gps -> gps.getPowerTypes().stream())
            .map(PowerType::getIdentifier)
            .collect(Collectors.toSet());
    }

    public static void applyGlobalPowers(Entity entity) {

        if (entity.getWorld().isClient) {
            return;
        }

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);
        if (component == null) {
            return;
        }

        List<GlobalPowerSet> globalPowerSets = getApplicableSets(entity.getType());
        Set<Identifier> powerTypeIds = getPowerTypeIds(globalPowerSets);

        boolean changed = removeExcessPowers(component, powerTypeIds);
        for (GlobalPowerSet globalPowerSet : globalPowerSets) {
            changed |= addMissingPowers(component, globalPowerSet);
        }

        if (changed) {
            component.sync();
        }

    }

    private static boolean removeExcessPowers(PowerHolderComponent phc, Set<Identifier> expected) {

        List<PowerType<?>> powersToRemove = phc.getPowersFromSource(POWER_SOURCE)
            .stream()
            .filter(p -> !expected.contains(p.getIdentifier()))
            .toList();

        powersToRemove.forEach(p -> phc.removePower(p, POWER_SOURCE));
        return !powersToRemove.isEmpty();

    }

    private static boolean addMissingPowers(PowerHolderComponent phc, GlobalPowerSet powerSet) {

        boolean added = false;
        for(PowerType<?> power : powerSet.getPowerTypes()) {
            if(!phc.hasPower(power, POWER_SOURCE)) {
                phc.addPower(power, POWER_SOURCE);
                added = true;
            }
        }

        return added;

    }
}
