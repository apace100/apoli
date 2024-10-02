package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record SyncPowersS2CPacket(Map<Identifier, Power> powersById) implements CustomPayload {

    public static final Id<SyncPowersS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/sync_power_registry"));
    public static final PacketCodec<RegistryByteBuf, SyncPowersS2CPacket> PACKET_CODEC = PacketCodec.of(SyncPowersS2CPacket::write, SyncPowersS2CPacket::read);

    public static SyncPowersS2CPacket read(RegistryByteBuf buf) {

        try {

            Collection<Power> powers = new ObjectArrayList<>();
            int powersCount = buf.readVarInt();

            for (int i = 0; i < powersCount; i++) {
                powers.add(Power.DATA_TYPE.receive(buf));
            }

            return new SyncPowersS2CPacket(powers
                .stream()
                .collect(Collectors.toMap(Power::getId, Function.identity(), (oldPower, newPower) -> newPower, Object2ObjectOpenHashMap::new)));

        }

        catch (Exception e) {
            Apoli.LOGGER.error(e.getMessage());
            throw e;
        }

    }

    public void write(RegistryByteBuf buf) {

        Collection<Power> powers = powersById().values();

        buf.writeVarInt(powers.size());
        powers.forEach(power -> Power.DATA_TYPE.send(buf, power));

    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
