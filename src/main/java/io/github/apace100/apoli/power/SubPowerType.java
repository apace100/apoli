package io.github.apace100.apoli.power;

import io.github.apace100.apoli.util.PowerPayloadType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;

public class SubPowerType extends PowerType {

    private final Identifier superPowerId;
    private final String subName;

    public SubPowerType(Identifier superPowerId, String subName, PowerType basePower) {
        super(basePower.getFactoryInstance(), basePower.getData());
        this.superPowerId = superPowerId;
        this.subName = subName;
    }

    @Override
    public void send(RegistryByteBuf buf) {

        buf.writeEnumConstant(PowerPayloadType.SUB_POWER);
        super.sendInternal(buf);

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
