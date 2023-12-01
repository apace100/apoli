package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.CustomToastData;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record ShowToastS2CPacket(CustomToastData toastData) implements FabricPacket {

    public static final PacketType<ShowToastS2CPacket> TYPE = PacketType.create(
        Apoli.identifier("s2c/show_toast"), ShowToastS2CPacket::read
    );

    public static ShowToastS2CPacket read(PacketByteBuf buf) {
        return new ShowToastS2CPacket(CustomToastData.DATA_TYPE.receive(buf));
    }

    @Override
    public void write(PacketByteBuf buf) {
        CustomToastData.DATA_TYPE.send(buf, toastData);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
