package io.github.apace100.apoli.power;

import com.google.common.collect.ImmutableSet;
import io.github.apace100.apoli.util.PowerPayloadType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.stream.Collectors;

public class MultiplePower extends Power {

    private final ImmutableSet<SubPower> subPowers;
    private final ImmutableSet<Identifier> subPowerIds;

    public MultiplePower(Power basePower, Set<Identifier> subPowerIds) {
        super(basePower.getFactoryInstance(), basePower.getData());
        this.subPowers = ImmutableSet.of();
        this.subPowerIds = ImmutableSet.copyOf(subPowerIds);
    }

    /**
     *  <b>This is only used when decoding multiple powers via {@link Power#DATA_TYPE}</b>
     */
    @ApiStatus.Internal
    public MultiplePower(Set<SubPower> subPowers, Power basePower) {
        super(basePower.getFactoryInstance(), basePower.getData());
        this.subPowers = ImmutableSet.copyOf(subPowers);
        this.subPowerIds = ImmutableSet.copyOf(this.subPowers
            .stream()
            .map(Power::getId)
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

    /**
     *  @return an immutable set of {@linkplain SubPower sub-powers}. <b>This should only be used when post-processing the sub-powers after decoding the multiple power with the {@link Power#DATA_TYPE}</b>
     */
    @ApiStatus.Internal
    public ImmutableSet<SubPower> getSubPowersInternal() {
        return subPowers;
    }

}
