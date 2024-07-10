package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;
import io.github.apace100.apoli.util.codec.SetCodec;
import net.minecraft.component.type.AttributeModifierSlot;

import java.util.Set;

public final class ApoliCodecs {

    public static final Codec<Set<AttributeModifierSlot>> ATTRIBUTE_MODIFIER_SLOT_SET = Codec.withAlternative(
        new SetCodec<>(AttributeModifierSlot.CODEC, 1, Integer.MAX_VALUE),
        AttributeModifierSlot.CODEC,
        Set::of
    );

}
