package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public record SyncPowerTypesS2CPacket(Map<Identifier, PowerType> powersById) implements CustomPayload {

    public static final Id<SyncPowerTypesS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/sync_power_types"));
    public static final PacketCodec<RegistryByteBuf, SyncPowerTypesS2CPacket> PACKET_CODEC = PacketCodec.of(SyncPowerTypesS2CPacket::write, SyncPowerTypesS2CPacket::read);

    public static SyncPowerTypesS2CPacket read(RegistryByteBuf buf) {

        Map<Identifier, PowerType> powersById = new HashMap<>();
        int count = buf.readVarInt();

        for (int i = 0; i < count; i++) {

            try {
                PowerType power = PowerType.receive(buf);
                powersById.put(power.getId(), power);
            }

            catch (Exception e) {
                Apoli.LOGGER.error(e.getMessage());
                throw e;
            }

        }

        return new SyncPowerTypesS2CPacket(powersById);

    }

    public void write(RegistryByteBuf buf) {

        Collection<PowerType> powers = this.powersById().values();

        buf.writeVarInt(powers.size());
        powers.forEach(power -> power.send(buf));

    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
