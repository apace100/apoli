package io.github.apace100.apoli.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.power.MultiplePower;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public sealed interface PowerReference {

	String asDisplayString();

	boolean isSubPower();

	default String asDisplayString(boolean capitalized) {
		String displayString = this.asDisplayString();
		return capitalized
			? displayString
			: StringUtils.uncapitalize(displayString);
	}

	static boolean isValidChar(char ch) {
		return ch == SubPower.SEPARATOR
			|| Identifier.isCharValid(ch);
	}

	static PowerReference ofPower(Identifier id) {
		return new Power(id);
	}

	static PowerReference ofSubPower(Identifier parentId, String name) {
		return new SubPower(parentId, name);
	}

	static PowerReference of(String value) {
		return ofValidated(value).getOrThrow(InvalidIdentifierException::new);
	}

	static DataResult<PowerReference> ofValidated(String value) {

		try {
			return DataResult.success(parse(new StringReader(value)));
		}

		catch (CommandSyntaxException cse) {
			return DataResult.error(cse::getMessage);
		}

	}

	static PowerReference parse(StringReader reader) throws CommandSyntaxException {

		int beginIndex = reader.getCursor();
		while (reader.canRead() && isValidChar(reader.peek())) {
			reader.skip();
		}

		String value = reader.getString().substring(beginIndex, reader.getCursor());
		int subSeparatorIndex = value.indexOf(SubPower.SEPARATOR);

		if (value.isEmpty()) {
			throw MiscUtil.createCommandException(() -> "Power references cannot be empty!");
		}

		else if (subSeparatorIndex >= 0) {

			String parent = value.substring(0, subSeparatorIndex);
			String name = value.substring(subSeparatorIndex + 1);

			if (parent.isEmpty()) {
				reader.setCursor(beginIndex);
				throw MiscUtil.createCommandExceptionWithContext(reader, () -> "Disallowed empty parent ID in sub-power reference \"" + value + "\"");
			}

			else if (name.isEmpty()) {
				reader.setCursor(beginIndex);
				throw MiscUtil.createCommandExceptionWithContext(reader, () -> "Disallowed empty name in sub-power reference \"" + value + "\"");
			}

			else {

				try {
					return ofSubPower(IdentifierUtil.nonEmptySplit(parent), name);
				}

				catch (InvalidIdentifierException iie) {
					reader.setCursor(beginIndex);
					throw MiscUtil.createCommandExceptionWithContext(reader, iie::getMessage);
				}

			}

		}

		else {

			try {
				return ofPower(IdentifierUtil.nonEmptySplit(value));
			}

			catch (InvalidIdentifierException iie) {
				reader.setCursor(beginIndex);
				throw MiscUtil.createCommandExceptionWithContext(reader, iie::getMessage);
			}

		}

	}

	record Power(Identifier id) implements PowerReference {

		public static Power of(Identifier id) {
			return new Power(id);
		}

		@Override
		public String asDisplayString() {
			return "Power \"" + id + "\"";
		}

		@Override
		public boolean isSubPower() {
			return false;
		}

		@Override
		public String toString() {
			return id().toString();
		}

		@Override
		public boolean equals(Object obj) {

			if (this == obj) {
				return true;
			}

			else if (obj instanceof Power that) {
				return Objects.equals(this.id(), that.id());
			}

			else {
				return false;
			}

		}

		@Override
		public int hashCode() {
			return Objects.hashCode(id());
		}

	}

	record SubPower(Identifier parentId, String name) implements PowerReference {

		public static final char SEPARATOR = '@';

		public SubPower {
			MultiplePower.validateSubPowerName(name).getOrThrow(IllegalArgumentException::new);
		}

		@Override
		public String asDisplayString() {
			return "Sub-power \"" + name() + "\" of power \"" + parentId() + "\"";
		}

		@Override
		public boolean isSubPower() {
			return true;
		}

		@Override
		public String toString() {
			return parentId().toString() + SEPARATOR + name();
		}

		@Override
		public boolean equals(Object obj) {

			if (this == obj) {
				return true;
			}

			else if (obj instanceof SubPower that) {
				return Objects.equals(this.parentId(), that.parentId())
					&& Objects.equals(this.name(), that.name());
			}

			else {
				return false;
			}

		}

		@Override
		public int hashCode() {
			return Objects.hash(this.parentId(), this.name());
		}

	}

}
