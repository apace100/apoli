package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.CustomToastData;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ShowToastS2CPacket(CustomToastData toastData) implements CustomPayload {

    public static final Id<ShowToastS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/show_toast"));
    public static final PacketCodec<RegistryByteBuf, ShowToastS2CPacket> PACKET_CODEC = PacketCodec.of(
        (value, buf) -> CustomToastData.DATA_TYPE.send(buf, value),
        buf -> new ShowToastS2CPacket(CustomToastData.DATA_TYPE.receive(buf))
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
