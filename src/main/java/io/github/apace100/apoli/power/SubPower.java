package io.github.apace100.apoli.power;

import io.github.apace100.apoli.util.PowerPayloadType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;

public class SubPower extends Power {

    private final Identifier superPowerId;
    private final String subName;

    SubPower(Identifier superPowerId, String subName, Power basePower) {
        super(basePower.getFactoryInstance(), basePower.data);
        this.superPowerId = superPowerId;
        this.subName = subName;
    }

    @Override
    public PowerPayloadType payloadType() {
        return PowerPayloadType.SUB_POWER;
    }

    @Override
    public void send(RegistryByteBuf buf) {

        super.send(buf);

        buf.writeIdentifier(this.getSuperPowerId());
        buf.writeString(this.getSubName());

    }

    public Identifier getSuperPowerId() {
        return superPowerId;
    }

    public String getSubName() {
        return subName;
    }

}
