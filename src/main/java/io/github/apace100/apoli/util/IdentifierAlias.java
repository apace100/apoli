package io.github.apace100.apoli.util;

import net.minecraft.util.Identifier;

/**
 *  A utility class for adding aliases to namespaces and paths of identifiers (or identifiers itself).
 */
public final class IdentifierAlias extends AliasSupplier {

    private static final RuntimeException NO_ALIAS = new RuntimeException("Tried to resolve an alias for an identifier which didn't have an alias.");

    public static void addAlias(Identifier fromId, Identifier toId) {
        addNamespaceAlias(fromId.getNamespace(), toId.getNamespace());
        addPathAlias(fromId.getPath(), toId.getPath());
    }

    public static boolean namespaceHasAlias(Identifier id) {
        return namespaceHasAlias(id.getNamespace());
    }

    public static boolean pathHasAlias(Identifier id) {
        return pathHasAlias(id.getPath());
    }

    public static boolean hasAlias(Identifier id) {
        return namespaceHasAlias(id)
            || pathHasAlias(id);
    }

    public static Identifier resolveAlias(Identifier id) {
        if (namespaceHasAlias(id) && pathHasAlias(id)) {
            return new Identifier(aliasedNamespaces.get(id.getNamespace()), aliasedPaths.get(id.getPath()));
        } else if (namespaceHasAlias(id) && !pathHasAlias(id)) {
            return resolveNamespaceAlias(id);
        } else if (!namespaceHasAlias(id) && pathHasAlias(id)) {
            return resolvePathAlias(id);
        } else {
            throw NO_ALIAS;
        }
    }

}
