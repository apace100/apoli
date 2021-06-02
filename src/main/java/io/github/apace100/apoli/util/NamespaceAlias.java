package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import net.minecraft.util.Identifier;

import java.util.HashSet;

public final class NamespaceAlias {

    private static HashSet<String> aliasedNamespaces = new HashSet<>();

    public static void addAlias(String namespace) {
        aliasedNamespaces.add(namespace);
    }

    public static boolean isAlias(String namespace) {
        return aliasedNamespaces.contains(namespace);
    }

    public static boolean isAlias(Identifier identifier) {
        return isAlias(identifier.getNamespace());
    }

    public static Identifier resolveAlias(Identifier original) {
        return new Identifier(Apoli.MODID, original.getPath());
    }
}
