package io.github.apace100.apoli.component;

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
import org.jetbrains.annotations.NotNull;

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

        Set<PowerType<?>> powerTypes = new HashSet<>(powers.keySet());
        if (!getSubPowerTypes) {

            for (PowerType<?> powerType : powers.keySet()) {

                if (!(powerType instanceof MultiplePowerType<?> multiplePowerType)) {
                    continue;
                }

                multiplePowerType.getSubPowers()
                    .stream()
                    .filter(PowerTypeRegistry::contains)
                    .map(PowerTypeRegistry::get)
                    .forEach(powerTypes::remove);

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

        StringBuilder errorMessage = new StringBuilder("Cannot remove a non-existing power");
        if (powerType instanceof PowerTypeReference<?> powerTypeRef) {
            powerType = powerTypeRef.getReferencedPowerType();
            errorMessage.append(" (ID: \"").append(powerType.getIdentifier()).append("\")");
        }

        if (powerType == null) {
            Apoli.LOGGER.error(errorMessage.append(" from entity ").append(owner.getName().getString()));
            return;
        }

        List<Identifier> sources = powerSources.getOrDefault(powerType, new LinkedList<>());
        if (sources.isEmpty()) {
            return;
        }

        sources.remove(source);
        if (sources.isEmpty()) {

            Power power = powers.remove(powerType);
            if (power != null) {

                power.onRemoved();
                power.onRemoved(false);

                power.onLost();

            }

        }

        if (powerType instanceof MultiplePowerType<?> multiplePowerType) {
            multiplePowerType.getSubPowers()
                .stream()
                .filter(PowerTypeRegistry::contains)
                .map(PowerTypeRegistry::get)
                .forEach(pt -> removePower(pt, source));
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

        StringBuilder errorMessage = new StringBuilder("Cannot add a non-existing power");
        if (powerType instanceof PowerTypeReference<?> powerTypeRef) {
            powerType = powerTypeRef.getReferencedPowerType();
            errorMessage.append(" (ID: \"").append(powerTypeRef.getIdentifier()).append("\")");
        }

        if (powerType == null) {
            Apoli.LOGGER.error(errorMessage.append(" to entity ").append(owner.getName().getString()));
            return false;
        }

        List<Identifier> sources = powerSources.computeIfAbsent(powerType, pt -> new LinkedList<>());
        if (sources.contains(source)) {
            return false;
        }

        sources.add(source);
        if (powerType instanceof MultiplePowerType<?> multiplePowerType) {
            multiplePowerType.getSubPowers()
                .stream()
                .filter(PowerTypeRegistry::contains)
                .map(PowerTypeRegistry::get)
                .forEach(pt -> addPower(pt, source));
        }

        Power power = powerType.create(owner);
        power.onGained();

        power.onAdded();
        power.onAdded(false);

        powerSources.put(powerType, sources);
        powers.put(powerType, power);

        if (owner instanceof ServerPlayerEntity player) {
            GainedPowerCriterion.INSTANCE.trigger(player, powerType);
        }

        return true;

    }

    @Override
    public void serverTick() {
        this.getPowers(Power.class, true).stream().filter(p -> p.shouldTick() && (p.shouldTickWhenInactive() || p.isActive())).forEach(Power::tick);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound compoundTag) {
        this.fromTag(compoundTag, true);
    }

    private void fromTag(NbtCompound compoundTag, boolean callPowerOnAdd) {

        if (owner == null) {
            Apoli.LOGGER.error("Owner was null in PowerHolderComponent#fromTag! This is not supposed to happen :(");
            return;
        }

        for (Power power : powers.values()) {

            if (callPowerOnAdd) {

                power.onRemoved();
                power.onLost();

            }

            power.onRemoved(!callPowerOnAdd);

        }

        powers.clear();
        NbtList powersTag = compoundTag.getList("Powers", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < powersTag.size(); i++) {

            NbtCompound powerTag = powersTag.getCompound(i);
            Identifier powerTypeId = Identifier.tryParse(powerTag.getString("Type"));

            if (powerTypeId == null || (callPowerOnAdd && PowerTypeRegistry.isDisabled(powerTypeId))) {
                continue;
            }

            List<Identifier> sources = new LinkedList<>();
            powerTag.getList("Sources", NbtElement.STRING_TYPE)
                .stream()
                .map(nbtElement -> Identifier.tryParse(nbtElement.asString()))
                .filter(Objects::nonNull)
                .forEach(sources::add);

            try {

                PowerType<?> powerType = PowerTypeRegistry.get(powerTypeId);
                Power power = powerType.create(owner);

                powerSources.put(powerType, sources);

                try {
                    NbtElement powerData = powerTag.get("Data");
                    power.fromTag(powerData);
                } catch (ClassCastException e) {
                    //  Occurs when the power was overridden by a data pack since last world load
                    //  where the overridden power now uses different data classes
                    Apoli.LOGGER.warn("Data type of power \"{}\" changed, skipping data for that power on entity {}", powerTypeId, owner.getName().getString());
                }

                powers.put(powerType, power);

                if (callPowerOnAdd) {
                    power.onAdded();
                }

                power.onAdded(!callPowerOnAdd);

            } catch (IllegalArgumentException e) {
                //  Occurs when the power is either not registered in the power registry,
                //  or the power no longer exists
                Apoli.LOGGER.warn("Unregistered power \"{}\" found on entity {}, skipping...", powerTypeId, owner.getName().getString());
            }

        }

        for (Map.Entry<PowerType<?>, List<Identifier>> entry : powerSources.entrySet()) {

            PowerType<?> powerType = entry.getKey();
            if (!(powerType instanceof MultiplePowerType<?> multiplePowerType)) {
                continue;
            }

            List<Identifier> subPowerIds = multiplePowerType.getSubPowers();
            for (Identifier subPowerId : subPowerIds) {
                try {

                    PowerType<?> subPowerType = PowerTypeRegistry.get(subPowerId);
                    for (Identifier source : entry.getValue()) {
                        addPower(subPowerType, source);
                    }

                } catch (IllegalArgumentException e) {
                    if (!(callPowerOnAdd && PowerTypeRegistry.isDisabled(subPowerId))) {
                        Apoli.LOGGER.warn("Multiple power \"{}\" (read from NBT data) contained unregistered sub-power: \"{}\"", powerType.getIdentifier(), subPowerId);
                    }
                }
            }

        }

    }

    @Override
    public void writeToNbt(@NotNull NbtCompound compoundTag) {

        NbtList powersTag = new NbtList();
        for (Map.Entry<PowerType<?>, Power> entry : powers.entrySet()) {

            PowerType<?> power = entry.getKey();
            NbtCompound powerTag = new NbtCompound();

            powerTag.putString("Factory", power.getFactory().getFactory().getSerializerId().toString());
            powerTag.putString("Type", power.getIdentifier().toString());
            powerTag.put("Data", entry.getValue().toTag());

            NbtList sourcesTag = new NbtList();
            powerSources.get(entry.getKey())
                .stream()
                .map(id -> NbtString.of(id.toString()))
                .forEach(sourcesTag::add);

            powerTag.put("Sources", sourcesTag);
            powersTag.add(powerTag);

        }

        compoundTag.put("Powers", powersTag);

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
