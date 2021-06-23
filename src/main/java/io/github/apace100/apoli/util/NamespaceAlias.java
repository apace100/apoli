package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public final class NamespaceAlias {

    private static final HashMap<String, String> aliasedNamespaces = new HashMap<>();

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
            throw new RuntimeException("Tried to resolve a namespace alias for a namespace which didn't have an alias.");
        }
        return new Identifier(aliasedNamespaces.get(original.getNamespace()), original.getPath());
    }
}
