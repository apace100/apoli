package io.github.apace100.apoli.util;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 *  A utility class for adding aliases to identifiers (or its namespaces and/or paths).
 */
//  TODO: Implement a priority system for the aliases
public final class IdentifierAlias {

    private static final RuntimeException NO_NAMESPACE_ALIAS = new RuntimeException("Tried to resolve a namespace alias for a namespace which didn't have an alias.");
    private static final RuntimeException NO_PATH_ALIAS = new RuntimeException("Tried to resolve a path alias for a path which didn't have an alias.");
    private static final RuntimeException NO_ALIAS = new RuntimeException("Tried to resolve an alias for an identifier which didn't have an alias.");

    private static final Map<String, String> aliasedNamespaces = new HashMap<>();
    private static final Map<String, String> aliasedPaths = new HashMap<>();

    public static void addNamespaceAlias(String fromNamespace, String toNamespace) {
        aliasedNamespaces.put(fromNamespace, toNamespace);
    }

    public static void addPathAlias(String fromPath, String toPath) {
        aliasedPaths.put(fromPath, toPath);
    }

    public static void addAlias(Identifier fromId, Identifier toId) {
        addNamespaceAlias(fromId.getNamespace(), toId.getNamespace());
        addPathAlias(fromId.getPath(), toId.getPath());
    }

    public static boolean namespaceHasAlias(Identifier id) {
        return namespaceHasAlias(id.getNamespace());
    }

    public static boolean namespaceHasAlias(String namespace) {
        return aliasedNamespaces.containsKey(namespace);
    }

    public static boolean pathHasAlias(Identifier id) {
        return pathHasAlias(id.getPath());
    }

    public static boolean pathHasAlias(String path) {
        return aliasedPaths.containsKey(path);
    }

    public static boolean hasAlias(Identifier id) {
        return namespaceHasAlias(id)
            || pathHasAlias(id);
    }

    public static Identifier resolveNamespaceAlias(Identifier id) {
        if (!namespaceHasAlias(id)) {
            throw NO_NAMESPACE_ALIAS;
        } else {
            return new Identifier(aliasedNamespaces.get(id.getNamespace()), id.getPath());
        }
    }

    public static Identifier resolvePathAlias(Identifier id) {
        if (!pathHasAlias(id)) {
            throw NO_PATH_ALIAS;
        } else {
            return new Identifier(id.getNamespace(), aliasedPaths.get(id.getPath()));
        }
    }

    public static Identifier resolveAlias(Identifier id) {
        if (namespaceHasAlias(id) && pathHasAlias(id)) {
            return new Identifier(aliasedNamespaces.get(id.getNamespace()), aliasedPaths.get(id.getPath()));
        } else if (namespaceHasAlias(id) && !pathHasAlias(id)) {
            return new Identifier(aliasedNamespaces.get(id.getNamespace()), id.getPath());
        } else if (!namespaceHasAlias(id) && pathHasAlias(id)) {
            return new Identifier(id.getNamespace(), aliasedPaths.get(id.getPath()));
        } else {
            throw NO_ALIAS;
        }
    }

}
