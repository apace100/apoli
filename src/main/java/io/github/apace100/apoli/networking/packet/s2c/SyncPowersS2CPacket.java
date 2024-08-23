package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public record SyncPowersS2CPacket(Map<Identifier, Power> powersById) implements CustomPayload {

    public static final Id<SyncPowersS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/sync_power_registry"));
    public static final PacketCodec<RegistryByteBuf, SyncPowersS2CPacket> PACKET_CODEC = PacketCodec.of(SyncPowersS2CPacket::write, SyncPowersS2CPacket::read);

    public static SyncPowersS2CPacket read(RegistryByteBuf buf) {

        Map<Identifier, Power> powersById = new HashMap<>();
        int count = buf.readVarInt();

        for (int i = 0; i < count; i++) {

            try {
                Power power = Power.receive(buf);
                powersById.put(power.getId(), power);
            }

            catch (Exception e) {
                Apoli.LOGGER.error(e.getMessage());
                throw e;
            }

        }

        return new SyncPowersS2CPacket(powersById);

    }

    public void write(RegistryByteBuf buf) {

        Collection<Power> powers = this.powersById().values();

        buf.writeVarInt(powers.size());
        powers.forEach(power -> power.send(buf));

    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
