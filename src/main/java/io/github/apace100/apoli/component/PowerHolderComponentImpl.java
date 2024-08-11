package io.github.apace100.apoli.component;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.MultiplePower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.GainedPowerCriterion;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PowerHolderComponentImpl implements PowerHolderComponent {

    private final LivingEntity owner;

    private final ConcurrentHashMap<Power, PowerType> powers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Power, List<Identifier>> powerSources = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Power, PowerType> powersToRemove = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Power, PowerType> powersToAdd = new ConcurrentHashMap<>();

    public PowerHolderComponentImpl(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean hasPower(Power power) {
        return powers.containsKey(power);
    }

    @Override
    public boolean hasPower(Power power, Identifier source) {
        return powerSources.containsKey(power) && powerSources.get(power).contains(source);
    }

    @Override
    public PowerType getPowerType(Power power) {
        return powers.get(power);
    }

    @Override
    public List<PowerType> getPowerTypes() {
        return new LinkedList<>(powers.values());
    }

    @Override
    public Set<Power> getPowers(boolean includeSubPowers) {
        return powers.keySet()
            .stream()
            .filter(pt -> includeSubPowers || !pt.isSubPower())
            .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public <T extends PowerType> List<T> getPowerTypes(Class<T> powerClass) {
        return getPowerTypes(powerClass, false);
    }

    @Override
    public <T extends PowerType> List<T> getPowerTypes(Class<T> powerClass, boolean includeInactive) {
        return powers.values()
            .stream()
            .filter(type -> powerClass.isAssignableFrom(type.getClass()))
            .map(powerClass::cast)
            .filter(type -> includeInactive || type.isActive())
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public List<Identifier> getSources(Power power) {

        if (powerSources.containsKey(power)) {
            return List.copyOf(powerSources.get(power));
        }

        else {
            return List.of();
        }

    }

    @Override
    public boolean removePower(Power power, Identifier source) {
        return this.removePower(power, source, true);
    }

    protected boolean removePower(Power power, Identifier source, boolean root) {

        StringBuilder errorMessage= new StringBuilder("Cannot remove a non-existing power");
        if (power instanceof PowerReference powerTypeReference) {
            errorMessage.append(" (ID: \")").append(power.getId()).append("\")");
            power = powerTypeReference.getReference();
        }

        if (power == null) {
            Apoli.LOGGER.error(errorMessage.append(" from entity ").append(owner.getName().getString()));
            return false;
        }

        if (!powerSources.containsKey(power)) {
            return false;
        }

        List<Identifier> sources = powerSources.get(power);
        sources.remove(source);

        if (sources.isEmpty() && powers.containsKey(power)) {

            PowerType powerType = powers.get(power);
            powersToRemove.put(power, powerType);

            powerType.onRemoved();
            powerType.onLost();

        }

        if (power instanceof MultiplePower multiplePowerType) {
            multiplePowerType.getSubPowers()
                .stream()
                .filter(PowerManager::contains)
                .map(PowerManager::get)
                .forEach(pt -> this.removePower(pt, source, false));
        }

        if (!root) {
            return true;
        }

        powers.keySet().removeIf(powersToRemove::containsKey);
        powerSources.keySet().removeIf(powersToRemove::containsKey);

        powersToRemove.clear();
        return true;

    }

    @Override
    public int removeAllPowersFromSource(Identifier source) {
        //noinspection MappingBeforeCount
        return (int) this.getPowersFromSource(source)
            .stream()
            .filter(Predicate.not(Power::isSubPower))
            .peek(pt -> this.removePower(pt, source))
            .count();
    }

    @Override
    public List<Power> getPowersFromSource(Identifier source) {
        return powerSources.entrySet()
            .stream()
            .filter(e -> e.getValue().contains(source))
            .map(Map.Entry::getKey)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public boolean addPower(Power power, Identifier source) {
        return this.addPower(power, source, true);
    }

    protected boolean addPower(Power power, Identifier source, boolean root) {

        StringBuilder errorMessage = new StringBuilder("Cannot add a non-existing power");
        if (power instanceof PowerReference powerTypeRef) {
            power = powerTypeRef.getReference();
            errorMessage.append(" (ID: \"").append(powerTypeRef.getId()).append("\")");
        }

        if (power == null) {
            Apoli.LOGGER.error(errorMessage.append(" to entity ").append(owner.getName().getString()));
            return false;
        }

        List<Identifier> sources = powerSources.computeIfAbsent(power, pt -> new LinkedList<>());
        if (sources.contains(source)) {
            return false;
        }

        PowerType powerType = power.create(owner);
        sources.add(source);

        powerSources.put(power, sources);
        powers.put(power, powerType);

        powersToAdd.put(power, powerType);

        if (power instanceof MultiplePower multiplePowerType) {
            multiplePowerType.getSubPowers()
                .stream()
                .filter(PowerManager::contains)
                .map(PowerManager::get)
                .forEach(pt -> this.addPower(pt, source, false));
        }

        if (!root) {
            return true;
        }

        Iterator<Map.Entry<Power, PowerType>> addedIterator = powersToAdd.entrySet().iterator();
        while (addedIterator.hasNext()) {

            Map.Entry<Power, PowerType> addedEntry = addedIterator.next();

            PowerType addedPowerType = addedEntry.getValue();
            addedIterator.remove();

            addedPowerType.onAdded();
            addedPowerType.onGained();

            if (owner instanceof ServerPlayerEntity player) {
                GainedPowerCriterion.INSTANCE.trigger(player, addedEntry.getKey());
            }

        }

        return true;

    }

    @Override
    public void serverTick() {
        this.getPowerTypes(PowerType.class, true)
            .stream()
            .filter(PowerType::shouldTick)
            .filter(type -> type.shouldTickWhenInactive() || type.isActive())
            .forEach(PowerType::tick);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound compoundTag, RegistryWrapper.WrapperLookup lookup) {
        this.fromTag(compoundTag, true);
    }

    protected void fromTag(NbtCompound compoundTag, boolean callPowerOnAdd) {

        if (owner == null) {
            Apoli.LOGGER.error("Owner was null in PowerHolderComponent#fromTag! This is not supposed to happen :(");
            return;
        }

        if (callPowerOnAdd) {
            powers.values().forEach(PowerType::onRemoved);
        }

        NbtList powersTag = compoundTag.getList("Powers", NbtElement.COMPOUND_TYPE);
        powers.clear();

        for (int i = 0; i < powersTag.size(); i++) {

            NbtCompound powerTag = powersTag.getCompound(i);
            Identifier powerTypeId = Identifier.tryParse(powerTag.getString("Type"));

            if (powerTypeId == null || (callPowerOnAdd && PowerManager.isDisabled(powerTypeId))) {
                continue;
            }

            List<Identifier> sources = powerTag.getList("Sources", NbtElement.STRING_TYPE)
                .stream()
                .map(NbtElement::asString)
                .map(Identifier::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedList::new));

            try {
                readPower(powerTag.get("Data"), powerTypeId, sources, callPowerOnAdd);
            }

            catch (IllegalArgumentException e) {
                //  Occurs when the power is either not registered in the power registry,
                //  or the power no longer exists
                Apoli.LOGGER.warn("Unregistered power \"{}\" found on entity {}, skipping...", powerTypeId, owner.getName().getString());
            }

        }

        for (Map.Entry<Power, List<Identifier>> entry : powerSources.entrySet()) {

            if (!(entry.getKey() instanceof MultiplePower multiplePower)) {
                continue;
            }

            for (Identifier subPowerId : multiplePower.getSubPowers()) {

                try {
                    readPower(null, subPowerId, entry.getValue(), callPowerOnAdd);
                }

                catch (IllegalArgumentException e) {

                    if (!(callPowerOnAdd && PowerManager.isDisabled(subPowerId))) {
                        Apoli.LOGGER.warn("Multiple power \"{}\" (read from NBT data) contained unregistered sub-power: \"{}\"", multiplePower.getId(), subPowerId);
                    }

                }

            }

        }

    }

    protected void readPower(@Nullable NbtElement powerData, Identifier powerTypeId, List<Identifier> sources, boolean callPowerOnAdd) {

        Power power = PowerManager.get(powerTypeId);
        PowerType powerType = power.create(owner);

        if (sources.isEmpty()) {
            Apoli.LOGGER.warn("Power \"{}\" with missing sources found on entity {}! Skipping...", powerTypeId, owner.getName().getString());
            return;
        }

        try {

            if (powerData != null) {
                powerType.fromTag(powerData);
            }

        }

        catch (ClassCastException e) {
            //  Occurs when the power was overridden by a data pack since last world load
            //  where the overridden power now uses different data classes
            Apoli.LOGGER.warn("Data type of power \"{}\" changed, skipping data for that power on entity {}", powerTypeId, owner.getName().getString());
        }

        powerSources.put(power, sources);
        powers.put(power, powerType);

        if (callPowerOnAdd) {
            powerType.onAdded();
        }

    }

    @Override
    public void writeToNbt(@NotNull NbtCompound compoundTag, RegistryWrapper.WrapperLookup wrapperLookup) {
        this.toTag(compoundTag);
    }

    private void toTag(NbtCompound compoundTag) {

        NbtList powersTag = new NbtList();
        for (Map.Entry<Power, PowerType> entry : powers.entrySet()) {

            Power power = entry.getKey();
            NbtCompound powerTag = new NbtCompound();

            if (!powerSources.containsKey(power) || powerSources.get(power).isEmpty()) {
                continue;
            }

            powerTag.putString("Factory", power.getFactoryInstance().getFactory().getSerializerId().toString());
            powerTag.putString("Type", power.getId().toString());
            powerTag.put("Data", entry.getValue().toTag());

            NbtList sourcesTag = powerSources.get(entry.getKey())
                .stream()
                .map(Identifier::toString)
                .map(NbtString::of)
                .collect(Collectors.toCollection(NbtList::new));

            powerTag.put("Sources", sourcesTag);
            powersTag.add(powerTag);

        }

        compoundTag.put("Powers", powersTag);

    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {

        NbtCompound compoundTag = new NbtCompound();
        this.toTag(compoundTag);

        buf.writeNbt(compoundTag);

    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {

        NbtCompound compoundTag = buf.readNbt();

        if (compoundTag != null) {
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
        for (Map.Entry<Power, PowerType> powerEntry : powers.entrySet()) {
            str.append("\t").append(powerEntry.getKey().getId()).append(": ").append(powerEntry.getValue().toTag().toString()).append("\n");
        }
        str.append("]");
        return str.toString();
    }

}
