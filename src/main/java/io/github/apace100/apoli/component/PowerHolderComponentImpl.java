package io.github.apace100.apoli.component;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.GainedPowerCriterion;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PowerHolderComponentImpl implements PowerHolderComponent {

    private final LivingEntity owner;
    private final ConcurrentHashMap<PowerType<?>, Power> powers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<PowerType<?>, List<Identifier>> powerSources = new ConcurrentHashMap<>();

    public PowerHolderComponentImpl(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean hasPower(PowerType<?> powerType) {
        return powers.containsKey(powerType);
    }

    @Override
    public boolean hasPower(PowerType<?> powerType, Identifier source) {
        return powerSources.containsKey(powerType) && powerSources.get(powerType).contains(source);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Power> T getPower(PowerType<T> powerType) {
        if(powers.containsKey(powerType)) {
            return (T)powers.get(powerType);
        }
        return null;
    }

    @Override
    public List<Power> getPowers() {
        return new LinkedList<>(powers.values());
    }

    public Set<PowerType<?>> getPowerTypes(boolean getSubPowerTypes) {
        HashSet<PowerType<?>> powerTypes = new HashSet<>(powers.keySet());
        for (PowerType<?> type : powers.keySet()) {
            if(!getSubPowerTypes && type instanceof MultiplePowerType<?>) {
                ((MultiplePowerType<?>)type).getSubPowers().stream().map(PowerTypeRegistry::get).forEach(powerTypes::remove);
            }
        }
        return powerTypes;
    }

    @Override
    public <T extends Power> List<T> getPowers(Class<T> powerClass) {
        return getPowers(powerClass, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Power> List<T> getPowers(Class<T> powerClass, boolean includeInactive) {
        List<T> list = new LinkedList<>();
        for(Power power : powers.values()) {
            if(powerClass.isAssignableFrom(power.getClass()) && (includeInactive || power.isActive())) {
                list.add((T)power);
            }
        }
        return list;
    }

    @Override
    public List<Identifier> getSources(PowerType<?> powerType) {
        if(powerSources.containsKey(powerType)) {
            return List.copyOf(powerSources.get(powerType));
        } else {
            return List.of();
        }
    }

    public void removePower(PowerType<?> powerType, Identifier source) {
        if(powerType instanceof PowerTypeReference<?>) {
            powerType = ((PowerTypeReference<?>)powerType).getReferencedPowerType();
        }
        if(powerSources.containsKey(powerType)) {
            List<Identifier> sources = powerSources.get(powerType);
            sources.remove(source);
            if(sources.isEmpty()) {
                powerSources.remove(powerType);
                Power power = powers.remove(powerType);
                if(power != null) {
                    power.onRemoved();
                    power.onLost();
                }
            }
            if(powerType instanceof MultiplePowerType) {
                ImmutableList<Identifier> subPowers = ((MultiplePowerType<?>)powerType).getSubPowers();
                for(Identifier subPowerId : subPowers) {
                    removePower(PowerTypeRegistry.get(subPowerId), source);
                }
            }
        }
    }

    @Override
    public int removeAllPowersFromSource(Identifier source) {
        List<PowerType<?>> powersToRemove = getPowersFromSource(source);
        powersToRemove.forEach(p -> removePower(p, source));
        return powersToRemove.size();
    }

    @Override
    public List<PowerType<?>> getPowersFromSource(Identifier source) {
        List<PowerType<?>> powers = new LinkedList<>();
        for(Map.Entry<PowerType<?>, List<Identifier>> sourceEntry : powerSources.entrySet()) {
            if(sourceEntry.getValue().contains(source)) {
                powers.add(sourceEntry.getKey());
            }
        }
        return powers;
    }

    public boolean addPower(PowerType<?> powerType, Identifier source) {
        if(powerType instanceof PowerTypeReference<?>) {
            powerType = ((PowerTypeReference<?>)powerType).getReferencedPowerType();
        }
        if(powerSources.containsKey(powerType)) {
            List<Identifier> sources = powerSources.get(powerType);
            if(sources.contains(source)) {
                return false;
            } else {
                sources.add(source);
                if(powerType instanceof MultiplePowerType) {
                    ImmutableList<Identifier> subPowers = ((MultiplePowerType<?>)powerType).getSubPowers();
                    for(Identifier subPowerId : subPowers) {
                        addPower(PowerTypeRegistry.get(subPowerId), source);
                    }
                }
                return true;
            }
        } else {
            List<Identifier> sources = new LinkedList<>();
            sources.add(source);
            if(powerType instanceof MultiplePowerType) {
                ImmutableList<Identifier> subPowers = ((MultiplePowerType<?>)powerType).getSubPowers();
                for(Identifier subPowerId : subPowers) {
                    addPower(PowerTypeRegistry.get(subPowerId), source);
                }
            }
            powerSources.put(powerType, sources);
            Power power = powerType.create(owner);
            this.powers.put(powerType, power);
            power.onGained();
            power.onAdded();
            if(owner instanceof ServerPlayerEntity spe) {
                GainedPowerCriterion.INSTANCE.trigger(spe, powerType);
            }
            return true;
        }
    }

    @Override
    public void serverTick() {
        this.getPowers(Power.class, true).stream().filter(p -> p.shouldTick() && (p.shouldTickWhenInactive() || p.isActive())).forEach(Power::tick);
    }

    @Override
    public void readFromNbt(NbtCompound compoundTag) {
        this.fromTag(compoundTag, true);
    }

    private void fromTag(NbtCompound compoundTag, boolean callPowerOnAdd) {
        try {
            if (owner == null) {
                Apoli.LOGGER.error("Owner was null in PowerHolderComponent#fromTag!");
            }
            if (callPowerOnAdd) {
                for (Power power : powers.values()) {
                    power.onRemoved();
                    power.onLost();
                }
            }
            powers.clear();
            NbtList powerList = (NbtList) compoundTag.get("Powers");
            if(powerList != null) {
                for (int i = 0; i < powerList.size(); i++) {
                    NbtCompound powerTag = powerList.getCompound(i);
                    Identifier powerTypeId = Identifier.tryParse(powerTag.getString("Type"));
                    if(callPowerOnAdd && PowerTypeRegistry.isDisabled(powerTypeId)) {
                        continue;
                    }
                    NbtList sources = (NbtList) powerTag.get("Sources");
                    List<Identifier> list = new LinkedList<>();
                    if(sources != null) {
                        sources.forEach(nbtElement -> list.add(Identifier.tryParse(nbtElement.asString())));
                    }
                    PowerType<?> type = PowerTypeRegistry.get(powerTypeId);
                    powerSources.put(type, list);
                    try {
                        NbtElement data = powerTag.get("Data");
                        Power power = type.create(owner);
                        try {
                            power.fromTag(data);
                        } catch (ClassCastException e) {
                            // Occurs when power was overriden by data pack since last world load
                            // to be a power type which uses different data class.
                            Apoli.LOGGER.warn("Data type of \"" + powerTypeId + "\" changed, skipping data for that power on entity " + owner.getName().getString());
                        }
                        this.powers.put(type, power);
                        if (callPowerOnAdd) {
                            power.onAdded();
                        }
                    } catch (IllegalArgumentException e) {
                        Apoli.LOGGER.warn("Power data of unregistered power \"" + powerTypeId + "\" found on entity, skipping...");
                    }
                }

                for(Map.Entry<PowerType<?>, List<Identifier>> entry : powerSources.entrySet()) {
                    PowerType<?> powerType = entry.getKey();
                    if(powerType instanceof MultiplePowerType) {
                        ImmutableList<Identifier> subPowers = ((MultiplePowerType<?>)powerType).getSubPowers();
                        for(Identifier subPowerId : subPowers) {
                            try {
                                PowerType<?> subType = PowerTypeRegistry.get(subPowerId);
                                for(Identifier source : entry.getValue()) {
                                    if(!hasPower(subType, source)) {
                                        addPower(subType, source);
                                    }
                                }
                            } catch (IllegalArgumentException e) {
                                if(callPowerOnAdd && PowerTypeRegistry.isDisabled(subPowerId)) {
                                    continue;
                                }
                                Apoli.LOGGER.warn("Multiple power type read from data contained unregistered sub-type: \"" + subPowerId + "\".");
                            }
                        }
                    }
                }
            }
        } catch(Exception e) {
            Apoli.LOGGER.error("Error while reading power holder data: " + e.getMessage());
        }
    }

    @Override
    public void writeToNbt(NbtCompound compoundTag) {
        NbtList powerList = new NbtList();
        for(Map.Entry<PowerType<?>, Power> powerEntry : powers.entrySet()) {
            NbtCompound powerTag = new NbtCompound();
            powerTag.putString("Type", PowerTypeRegistry.getId(powerEntry.getKey()).toString());
            powerTag.put("Data", powerEntry.getValue().toTag());
            NbtList sources = new NbtList();
            powerSources.get(powerEntry.getKey()).forEach(id -> sources.add(NbtString.of(id.toString())));
            powerTag.put("Sources", sources);
            powerList.add(powerTag);
        }
        compoundTag.put("Powers", powerList);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        NbtCompound compoundTag = buf.readNbt();
        if(compoundTag != null) {
            this.fromTag(compoundTag, false);
        }
    }

    @Override
    public void sync() {
        PowerHolderComponent.sync(this.owner);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("PowerHolderComponent[\n");
        for (Map.Entry<PowerType<?>, Power> powerEntry : powers.entrySet()) {
            str.append("\t").append(PowerTypeRegistry.getId(powerEntry.getKey())).append(": ").append(powerEntry.getValue().toTag().toString()).append("\n");
        }
        str.append("]");
        return str.toString();
    }
}
