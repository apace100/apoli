package io.github.apace100.apoli.util;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

abstract class AliasSupplier {

    protected static final RuntimeException NO_NAMESPACE_ALIAS = new RuntimeException("Tried to resolve a namespace alias for a namespace which didn't have an alias.");
    protected static final RuntimeException NO_PATH_ALIAS = new RuntimeException("Tried to resolve a path alias for a path which didn't have an alias.");

    protected static final Map<String, String> aliasedNamespaces = new HashMap<>();
    protected static final Map<String, String> aliasedPaths = new HashMap<>();

    public static void addNamespaceAlias(String fromNamespace, String toNamespace) {
        aliasedNamespaces.put(fromNamespace, toNamespace);
    }

    public static void addPathAlias(String fromPath, String toPath) {
        aliasedPaths.put(fromPath, toPath);
    }

    public static boolean namespaceHasAlias(String namespace) {
        return aliasedNamespaces.containsKey(namespace);
    }

    public static boolean pathHasAlias(String path) {
        return aliasedPaths.containsKey(path);
    }

    public static Identifier resolveNamespaceAlias(Identifier id) {
        if (!aliasedNamespaces.containsKey(id.getNamespace())) {
            throw NO_NAMESPACE_ALIAS;
        } else {
            return new Identifier(aliasedNamespaces.get(id.getNamespace()), id.getPath());
        }
    }

    public static Identifier resolvePathAlias(Identifier id) {
        if (!aliasedPaths.containsKey(id.getPath())) {
            throw NO_PATH_ALIAS;
        } else {
            return new Identifier(id.getNamespace(), aliasedPaths.get(id.getPath()));
        }
    }

}
