package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;
import io.github.apace100.apoli.util.codec.SetCodec;
import net.minecraft.component.type.AttributeModifierSlot;

import java.util.EnumSet;
import java.util.function.Function;

public final class ApoliCodecs {

    public static final Codec<EnumSet<AttributeModifierSlot>> ATTRIBUTE_MODIFIER_SLOT_SET = Codec.withAlternative(
        new SetCodec<>(AttributeModifierSlot.CODEC, 1, AttributeModifierSlot.values().length).xmap(EnumSet::copyOf, Function.identity()),
        AttributeModifierSlot.CODEC,
        EnumSet::of
    );

}
