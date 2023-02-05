package io.github.apace100.apoli.global;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalPowerSetUtil {

    public static Identifier POWER_SOURCE = Apoli.identifier("global");

    public static List<GlobalPowerSet> getApplicableSets(EntityType<?> type) {
        List<GlobalPowerSet> result = new LinkedList<>();
        Iterator<GlobalPowerSet> sets = GlobalPowerSetLoader.ALL.iterator();
        while(sets.hasNext()) {
            GlobalPowerSet gps = sets.next();
            if(gps.doesApply(type)) {
                result.add(gps);
            }
        }
        result.sort(GlobalPowerSet::compareTo);
        return result;
    }

    public static Set<Identifier> getPowerTypeIds(List<GlobalPowerSet> powerSets) {
        return powerSets.stream()
                .flatMap(gps -> gps.getPowerTypes().stream())
                .map(PowerType::getIdentifier)
                .collect(Collectors.toSet());
    }

    public static void applyGlobalPowers(Entity entity) {
        Optional<PowerHolderComponent> optional = PowerHolderComponent.KEY.maybeGet(entity);
        if(optional.isEmpty()) {
            return;
        }
        PowerHolderComponent phc = optional.get();
        List<GlobalPowerSet> sets = getApplicableSets(entity.getType());
        Set<Identifier> ids = getPowerTypeIds(sets);
        boolean change = removeExcessPowers(phc, ids);
        for(GlobalPowerSet powerSet : sets) {
            change |= addMissingPowers(phc, powerSet);
        }
        if(change) {
            phc.sync();
        }
    }

    private static boolean removeExcessPowers(PowerHolderComponent phc, Set<Identifier> expected) {
        List<PowerType<?>> powers = phc.getPowersFromSource(POWER_SOURCE);
        List<PowerType<?>> toRemove = new LinkedList<>();
        for(PowerType<?> pt : powers) {
            Identifier id = pt.getIdentifier();
            if(!expected.contains(id)) {
                toRemove.add(pt);
            }
        }
        for(PowerType<?> pt : toRemove) {
            phc.removePower(pt, POWER_SOURCE);
        }
        return toRemove.size() > 0;
    }

    private static boolean addMissingPowers(PowerHolderComponent phc, GlobalPowerSet powerSet) {
        boolean added = false;
        for(PowerType<?> pt : powerSet.getPowerTypes()) {
            if(!phc.hasPower(pt, POWER_SOURCE)) {
                phc.addPower(pt, POWER_SOURCE);
                added = true;
            }
        }
        return added;
    }
}
