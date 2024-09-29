package io.github.apace100.apoli.text;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.mixin.TranslatableTextContentAccessor;
import net.minecraft.text.*;
import net.minecraft.util.Language;

import java.util.List;
import java.util.Optional;

public class ForcedTranslatableTextContent extends TranslatableTextContent {

	public static final MapCodec<ForcedTranslatableTextContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Codec.STRING.fieldOf("translate").forGetter(ForcedTranslatableTextContent::getKey),
		TextCodecs.CODEC.fieldOf("alt_text").forGetter(ForcedTranslatableTextContent::getTextFallback),
		TranslatableTextContentAccessor.getArgumentCodec().listOf().optionalFieldOf("with").forGetter(content -> TranslatableTextContentAccessor.callToOptionalList(content.getArgs()))
	).apply(instance, ForcedTranslatableTextContent::new));

	public static final Type<ForcedTranslatableTextContent> TYPE = new Type<>(CODEC, "apoli:forced_translatable");

	private final Text textFallback;

	public ForcedTranslatableTextContent(String key, Text textFallback, Object... args) {
		super(key, null, args);
		this.textFallback = textFallback;
	}

	private ForcedTranslatableTextContent(String key, Text textFallback, Optional<List<Object>> args) {
		this(key, textFallback, TranslatableTextContentAccessor.callToArray(args));
	}

	@Override
	public Type<?> getType() {
		return TYPE;
	}

	@Override
	protected void updateTranslations() {

		Language language = Language.getInstance();
		if (language == this.languageCache) {
			return;
		}

		String key = this.getKey();
		String translated = language.get(key);

		this.languageCache = language;

		if (language.hasTranslation(key)) {

			try {

				ImmutableList.Builder<StringVisitable> builder = ImmutableList.builder();
				((TranslatableTextContentAccessor) this).callForEachPart(translated, builder::add);

				this.translations = builder.build();

			}

			catch (TranslationException te) {
				this.translations = ImmutableList.of(StringVisitable.plain(translated));
			}

		}

		else {
			this.translations = ImmutableList.of(getTextFallback());
		}

	}

	public Text getTextFallback() {
		return textFallback;
	}

}
