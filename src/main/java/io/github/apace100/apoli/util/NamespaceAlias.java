package io.github.apace100.apoli.util;

import net.minecraft.util.Identifier;

/**
 *  A utility class for adding aliases to namespaces. This is <b>deprecated</b>, use {@link IdentifierAlias} instead.
 */
@Deprecated(forRemoval = true)
public final class NamespaceAlias extends AliasSupplier {

    public static void addAlias(String fromNamespace, String toNamespace) {
        aliasedNamespaces.put(fromNamespace, toNamespace);
    }

    public static boolean hasAlias(String namespace) {
        return aliasedNamespaces.containsKey(namespace);
    }

    public static boolean hasAlias(Identifier identifier) {
        return hasAlias(identifier.getNamespace());
    }

    public static Identifier resolveAlias(Identifier original) {
        if(!aliasedNamespaces.containsKey(original.getNamespace())) {
            throw NO_NAMESPACE_ALIAS;
        }
        return new Identifier(aliasedNamespaces.get(original.getNamespace()), original.getPath());
    }
}
