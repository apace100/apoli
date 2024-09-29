package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.codec.CalioPacketCodecs;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public record SyncPowersS2CPacket(Map<Identifier, Power> powersById) implements CustomPayload {

    public static final Id<SyncPowersS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/sync_power_registry"));
    public static final PacketCodec<RegistryByteBuf, SyncPowersS2CPacket> PACKET_CODEC = PacketCodec.of(SyncPowersS2CPacket::write, SyncPowersS2CPacket::read);

    private static final PacketCodec<RegistryByteBuf, Collection<Power>> POWERS_PACKET_CODEC = CalioPacketCodecs.collection(ObjectArrayList::new, Power.DATA_TYPE::packetCodec);

    public static SyncPowersS2CPacket read(RegistryByteBuf buf) {

        try {
            return new SyncPowersS2CPacket(POWERS_PACKET_CODEC.decode(buf)
                .stream()
                .map(power -> Map.entry(power.getId(), power))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldP, newP) -> newP, Object2ObjectLinkedOpenHashMap::new)));
        }

        catch (Exception e) {
            Apoli.LOGGER.error(e);
            throw e;
        }

    }

    public void write(RegistryByteBuf buf) {
        POWERS_PACKET_CODEC.encode(buf, powersById().values());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
