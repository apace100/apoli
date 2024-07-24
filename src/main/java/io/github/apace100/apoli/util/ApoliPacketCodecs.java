package io.github.apace100.apoli.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.EnumSet;

public final class ApoliPacketCodecs {

    public static final PacketCodec<ByteBuf, EnumSet<AttributeModifierSlot>> ATTRIBUTE_MODIFIER_SLOT_SET = ApoliCodecUtil.withAlternativePacketCodec(
        PacketCodecs.collection(size -> EnumSet.noneOf(AttributeModifierSlot.class), AttributeModifierSlot.PACKET_CODEC),
        AttributeModifierSlot.PACKET_CODEC,
        EnumSet::of
    );

}
