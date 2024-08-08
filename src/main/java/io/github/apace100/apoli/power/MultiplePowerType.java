package io.github.apace100.apoli.power;

import com.google.common.collect.ImmutableSet;
import io.github.apace100.apoli.util.PowerPayloadType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.stream.Collectors;

public class MultiplePowerType extends PowerType {

    private final ImmutableSet<SubPowerType> subPowers;
    private final ImmutableSet<Identifier> subPowerIds;

    public MultiplePowerType(PowerType basePower, Set<Identifier> subPowerIds) {
        super(basePower.getFactoryInstance(), basePower.getData());
        this.subPowers = ImmutableSet.of();
        this.subPowerIds = ImmutableSet.copyOf(subPowerIds);
    }

    public MultiplePowerType(Set<SubPowerType> subPowers, PowerType basePower) {
        super(basePower.getFactoryInstance(), basePower.getData());
        this.subPowers = ImmutableSet.copyOf(subPowers);
        this.subPowerIds = ImmutableSet.copyOf(this.subPowers
            .stream()
            .map(PowerType::getId)
            .collect(Collectors.toSet()));
    }

    @Override
    public void send(RegistryByteBuf buf) {

        buf.writeEnumConstant(PowerPayloadType.MULTIPLE_POWER);

        super.sendInternal(buf);
        buf.writeCollection(this.getSubPowers(), PacketByteBuf::writeIdentifier);

    }

    public ImmutableSet<Identifier> getSubPowers() {
        return subPowerIds;
    }

    @ApiStatus.Internal
    public ImmutableSet<SubPowerType> getSubPowersInternal() {
        return subPowers;
    }

}
