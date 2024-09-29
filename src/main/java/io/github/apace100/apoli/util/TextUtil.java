package io.github.apace100.apoli.util;

import io.github.apace100.apoli.text.ForcedTranslatableTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

import java.util.Optional;

public class TextUtil {

	/**
	 * 	<p>Forcefully constructs a translatable text out of the {@code translationKey} and {@code altText} arguments.
	 * 	The resulting translatable text may differ according to the following scenarios:</p>
	 *
	 * 	<ol>
	 * 	    <li>
	 * 	        If {@code altText} is empty, a traditional translatable text will be constructed
	 * 	        (via {@link Text#translatable(String)}.)
	 * 	    </li>
	 * 	    <li>
	 * 	        If {@code altText} is already a translatable text, it will be used as is.
	 * 	    </li>
	 * 	    <li>
	 * 	        If {@code altText} is a literal string, the string will be used as a fallback translation for the
	 * 	        translatable text (constructed via {@link Text#translatableWithFallback(String, String)}.)
	 * 	    </li>
	 * 	    <li>
	 * 	        If neither of the above scenarios are inapplicable, a {@linkplain ForcedTranslatableTextContent forced
	 * 	        translatable text} will be constructed with {@code altText} serving as its fallback text.
	 * 	    </li>
	 * 	</ol>
	 *
	 * @param translationKey	the key of the translatable text that will be translated
	 * @param altText			the text to use as a fallback if a traditional translatable text can't be constructed
	 *
	 * @return	either a traditional translatable text (if {@code altText} is present, and a literal string, or a
	 * 			translatable text), or a {@link ForcedTranslatableTextContent}.
	 */
	public static Text forceTranslatable(String translationKey, Optional<Text> altText) {

		if (altText.isPresent()) {

			Text text = altText.get();
			String literal = text.getLiteralString();

			if (text.getContent() instanceof TranslatableTextContent) {
				return text;
			}

			else if (literal != null) {
				return Text.translatableWithFallback(translationKey, literal);
			}

			else {
				return MutableText.of(new ForcedTranslatableTextContent(translationKey, text));
			}

		}

		else {
			return Text.translatable(translationKey);
		}

	}

}
