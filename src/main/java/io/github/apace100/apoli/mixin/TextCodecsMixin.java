package io.github.apace100.apoli.mixin;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import io.github.apace100.apoli.text.ForcedTranslatableTextContent;
import net.minecraft.text.TextCodecs;
import net.minecraft.text.TextContent;
import net.minecraft.util.StringIdentifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(TextCodecs.class)
public abstract class TextCodecsMixin {

	@ModifyArg(method = "createCodec", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/TextCodecs;dispatchingCodec([Lnet/minecraft/util/StringIdentifiable;Ljava/util/function/Function;Ljava/util/function/Function;Ljava/lang/String;)Lcom/mojang/serialization/MapCodec;"))
	private static StringIdentifiable[] apoli$addCustomTypes(StringIdentifiable[] original) {

		if (original.getClass().getComponentType().isAssignableFrom(TextContent.Type.class)) {

			TextContent.Type<?>[] copy = (TextContent.Type<?>[]) Arrays.copyOf(original, original.length + 1);
			copy[copy.length - 1] = ForcedTranslatableTextContent.TYPE;

			return copy;

		}

		else {
			return original;
		}

	}

	@Mixin(targets = "net/minecraft/text/TextCodecs$FuzzyCodec")
	public static abstract class FuzzyCodecMixin<T> {

		@Inject(method = "encode", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/MapEncoder;encode(Ljava/lang/Object;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/RecordBuilder;)Lcom/mojang/serialization/RecordBuilder;"))
		private <S> void apoli$encodeType(T input, DynamicOps<S> ops, RecordBuilder<S> prefix, CallbackInfoReturnable<RecordBuilder<S>> cir) {

			//	Encode the text content's type to the result. This ensures that when sending a text to the client,
			//	the client will know what text content type to use to decode instead of relying on fuzzy matching
			if (input instanceof TextContent textContent) {
				prefix.add("type", ops.createString(textContent.getType().id()));
			}

		}

	}

}
