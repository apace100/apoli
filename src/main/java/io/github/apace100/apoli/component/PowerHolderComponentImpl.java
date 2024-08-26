package io.github.apace100.apoli.component;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.MultiplePower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.GainedPowerCriterion;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PowerHolderComponentImpl implements PowerHolderComponent {

    private final ConcurrentHashMap<Power, PowerType> powers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Power, List<Identifier>> powerSources = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Power, PowerType> powersToRemove = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Power, PowerType> powersToAdd = new ConcurrentHashMap<>();

    private final LivingEntity owner;

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
    public <T extends PowerType> List<T> getPowerTypes(Class<T> typeClass) {
        return getPowerTypes(typeClass, false);
    }

    @Override
    public <T extends PowerType> List<T> getPowerTypes(Class<T> typeClass, boolean includeInactive) {
        return powers.values()
            .stream()
            .filter(typeClass::isInstance)
            .map(typeClass::cast)
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

        if (power instanceof MultiplePower multiplePower) {
            multiplePower.getSubPowers().forEach(subPower -> this.removePower(subPower, source, false));
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

        if (power instanceof MultiplePower multiplePower) {
            multiplePower.getSubPowers().forEach(subPower -> this.addPower(subPower, source, false));
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

        powers.clear();
        NbtList powersTag = compoundTag.getList("powers", NbtElement.COMPOUND_TYPE);

        //  Migrate compound NBTs from the old 'Powers' NBT path to the new 'powers' NBT path
        if (compoundTag.contains("Powers")) {
            powersTag.addAll(compoundTag.getList("Powers", NbtElement.COMPOUND_TYPE));
        }

        for (int i = 0; i < powersTag.size(); i++) {

            NbtCompound powerTag = powersTag.getCompound(i);

            try {

                Power.Entry powerEntry = Power.Entry.CODEC.strictParse(lookup.getOps(NbtOps.INSTANCE), powerTag);
                Identifier powerId = powerEntry.power().getId();

                try {

                    Power power = powerEntry.power().getStrictReference();
                    PowerType powerType = power.create(owner);

                    try {

                        NbtElement powerData = powerEntry.nbtData();

                        if (powerData != null) {
                            powerType.fromTag(powerData);
                        }

                    }

                    catch (ClassCastException cce) {
                        //  Occurs when the power was overridden by a data pack since last resource reload,
                        //  where the overridden power may encode/decode different NBT types
                        Apoli.LOGGER.warn("Data type of power \"{}\" has changed, skipping data for that power on entity {} (UUID: {})", powerId, owner.getName().getString(), owner.getUuidAsString());
                    }

                    powerSources.put(power, powerEntry.sources());
                    powers.put(power, powerType);

                }

                catch (Throwable t) {
                    Apoli.LOGGER.warn("Unregistered power \"{}\" found on entity {} (UUID: {}), skipping...", powerId, owner.getName().getString(), owner.getUuidAsString());
                }

            }

            catch (Throwable t) {
                Apoli.LOGGER.warn("Error trying to decode NBT element ({}) at index {} into a power from NBT of entity {} (UUID: {}) (skipping): {}", powerTag, i, owner.getName().getString(), owner.getUuidAsString(), t.getMessage());
            }

        }

    }

    @Override
    public void writeToNbt(@NotNull NbtCompound compoundTag, RegistryWrapper.WrapperLookup wrapperLookup) {

        NbtList powersTag = new NbtList();
        powers.forEach((power, powerType) -> {

            try {

                Power.Entry powerEntry = new Power.Entry(
                    power.getFactoryInstance().getFactory(),
                    new PowerReference(power.getId()),
                    powerType.toTag(),
                    powerSources.get(power)
                );

                powersTag.add(Power.Entry.CODEC.strictEncodeStart(NbtOps.INSTANCE, powerEntry));

            }

            catch (Throwable t) {
                Apoli.LOGGER.warn("Error encoding power \"{}\" to NBT of entity {} (UUID: {}) (skipping): {}", power.getId(), owner.getName().getString(), owner.getUuidAsString(), t.getMessage());
            }

        });

        compoundTag.put("powers", powersTag);

    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeVarInt(0);
        PowerHolderComponent.super.writeSyncPacket(buf, recipient);
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {

        int syncType = buf.readVarInt();
        switch (syncType) {
            case 0 ->
                PowerHolderComponent.super.applySyncPacket(buf);
            case 1 ->
                PacketHandlers.GRANT_POWERS.apply(buf, this);
            case 2 ->
                PacketHandlers.REVOKE_POWERS.apply(buf, this);
            case 3 ->
                PacketHandlers.REVOKE_ALL_POWERS.apply(buf, this);
            default ->
                Apoli.LOGGER.warn("Received unknown sync type with ID {} (expected value range: [0 to 3]) when applying sync packet to entity {}! Skipping...", syncType, owner.getName().getString());
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
