package io.github.apace100.apoli.power;

import com.google.common.collect.ImmutableSet;
import io.github.apace100.apoli.util.PowerPayloadType;
import net.minecraft.network.RegistryByteBuf;

import java.util.Set;

public class MultiplePower extends Power {

    private final ImmutableSet<SubPower> subPowers;

    protected MultiplePower(Power basePower, Set<SubPower> subPowers) {
        super(basePower.getFactoryInstance(), basePower.data);
        this.subPowers = ImmutableSet.copyOf(subPowers);
    }

    @Override
    public PowerPayloadType payloadType() {
        return PowerPayloadType.MULTIPLE_POWER;
    }

    @Override
    public void send(RegistryByteBuf buf) {

        super.send(buf);

        buf.writeVarInt(subPowers.size());
        subPowers.forEach(subPower -> subPower.send(buf));

    }

    public Set<SubPower> getSubPowers() {
        return subPowers;
    }

}
