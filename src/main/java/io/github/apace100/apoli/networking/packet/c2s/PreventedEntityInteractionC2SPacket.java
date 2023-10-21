package io.github.apace100.apoli.networking.packet.c2s;

import io.github.apace100.apoli.Apoli;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;

public record PreventedEntityInteractionC2SPacket(int interactedEntityId, Hand hand) implements FabricPacket {

    public static final PacketType<PreventedEntityInteractionC2SPacket> TYPE = PacketType.create(
        Apoli.identifier("c2s/prevented_entity_interaction"), PreventedEntityInteractionC2SPacket::read
    );

    private static PreventedEntityInteractionC2SPacket read(PacketByteBuf buffer) {
        Hand hand = Hand.values()[buffer.readVarInt()];
        return new PreventedEntityInteractionC2SPacket(buffer.readVarInt(), hand);
    }

    @Override
    public void write(PacketByteBuf buffer) {
        buffer.writeVarInt(hand.ordinal());
        buffer.writeVarInt(interactedEntityId);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
