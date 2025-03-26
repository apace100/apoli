package io.github.apace100.apoli.util;

import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

public class IdentifierUtil {

	public static Identifier nonEmptySplit(String value) {

		if (value.isEmpty()) {
			throw new InvalidIdentifierException("Empty resource locations are not allowed!");
		}

		else {

			String namespace = Identifier.DEFAULT_NAMESPACE;
			String path = value;

			int separatorIndex = value.indexOf(Identifier.NAMESPACE_SEPARATOR);
			if (separatorIndex > 0) {
				namespace = value.substring(0, separatorIndex);
				path = value.substring(separatorIndex + 1);
			}

			return nonEmptyOf(namespace, path);

		}

	}

	public static Identifier nonEmptyOf(String namespace, String path) {

		boolean emptyNamespace = namespace.isEmpty();
		boolean emptyPath = path.isEmpty();

		if (emptyNamespace || emptyPath) {
			throw new InvalidIdentifierException("Disallowed empty " + (emptyNamespace ? "namespace" : "path") + " in resource location \"" + (namespace + Identifier.NAMESPACE_SEPARATOR + path) + "\"");
		}

		else {
			return Identifier.of(namespace, path);
		}

	}

}
