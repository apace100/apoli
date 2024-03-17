package io.github.apace100.apoli.util;

import net.minecraft.util.Identifier;

/**
 *  A utility class for adding aliases to namespaces. This is <b>deprecated</b>, use {@link IdentifierAlias} instead.
 */
@Deprecated(forRemoval = true)
public final class NamespaceAlias {

    public static void addAlias(String fromNamespace, String toNamespace) {
        IdentifierAlias.addNamespaceAlias(fromNamespace, toNamespace);
    }

    public static boolean hasAlias(String namespace) {
        return IdentifierAlias.namespaceHasAlias(namespace);
    }

    public static boolean hasAlias(Identifier identifier) {
        return hasAlias(identifier.getNamespace());
    }

    public static Identifier resolveAlias(Identifier original) {
        return IdentifierAlias.resolveNamespaceAlias(original);
    }

}
