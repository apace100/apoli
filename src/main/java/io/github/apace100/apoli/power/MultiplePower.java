package io.github.apace100.apoli.power;

import com.google.common.collect.ImmutableSet;
import io.github.apace100.apoli.util.PowerPayloadType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MultiplePower extends Power {

    private ImmutableSet<Identifier> subPowerIds = ImmutableSet.of();

    MultiplePower(Power basePower, Set<Identifier> subPowerIds) {
        this(basePower);
        this.subPowerIds = ImmutableSet.copyOf(subPowerIds);
    }

    MultiplePower(Power basePower) {
        super(basePower.getFactoryInstance(), basePower.data);
    }

    @Override
    public PowerPayloadType payloadType() {
        return PowerPayloadType.MULTIPLE_POWER;
    }

    @Override
    public void send(RegistryByteBuf buf) {
        super.send(buf);
        buf.writeCollection(subPowerIds, PacketByteBuf::writeIdentifier);
    }

    public ImmutableSet<Identifier> getSubPowerIds() {
        return subPowerIds;
    }

    void setSubPowerIds(Set<Identifier> subPowerIds) {
        this.subPowerIds = ImmutableSet.copyOf(subPowerIds);
    }

    public Set<SubPower> getSubPowers() {
        return this.getSubPowerIds()
            .stream()
            .filter(PowerManager::contains)
            .map(PowerManager::get)
            .filter(SubPower.class::isInstance)
            .map(SubPower.class::cast)
            .collect(Collectors.toCollection(HashSet::new));
    }

}
