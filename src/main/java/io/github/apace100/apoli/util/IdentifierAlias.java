package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 *  A utility class for adding aliases to identifiers (or its namespaces and/or paths).
 */
public final class IdentifierAlias {

    private static final RuntimeException NO_NAMESPACE_ALIAS = new RuntimeException("Tried to resolve a namespace alias for a namespace which didn't have an alias.");
    private static final RuntimeException NO_PATH_ALIAS = new RuntimeException("Tried to resolve a path alias for a path which didn't have an alias.");
    private static final RuntimeException NO_ALIAS = new RuntimeException("Tried to resolve an alias for an identifier which didn't have an alias.");

    private static final Map<Identifier, Identifier> ALIASED_IDENTIFIERS = new HashMap<>();
    private static final Map<String, String> ALIASED_NAMESPACES = new HashMap<>();
    private static final Map<String, String> ALIASED_PATHS = new HashMap<>();

    public enum Priority {
        IDENTIFIER,
        NAMESPACE,
        PATH
    }

    public static void addNamespaceAlias(String fromNamespace, String toNamespace) {

        if (namespaceHasAlias(fromNamespace)) {
            Apoli.LOGGER.error("[{}] Cannot add alias \"{}\" to namespace \"{}\", as it already exists!", Apoli.MODID, fromNamespace, toNamespace);
            return;
        }

        ALIASED_NAMESPACES.put(fromNamespace, toNamespace);

    }

    public static void addPathAlias(String fromPath, String toPath) {

        if (pathHasAlias(fromPath)) {
            Apoli.LOGGER.error("[{}] Cannot add alias \"{}\" to path \"{}\", as it already exists!", Apoli.MODID, fromPath, toPath);
            return;
        }

        ALIASED_PATHS.put(fromPath, toPath);

    }

    public static void addAlias(Identifier fromId, Identifier toId) {

        if (identifierHasAlias(fromId)) {
            Apoli.LOGGER.error("[{}] Cannot add alias \"{}\" to identifier \"{}\", as it already exists!", Apoli.MODID, fromId, toId);
            return;
        }

        ALIASED_IDENTIFIERS.put(fromId, toId);

    }

    public static boolean identifierHasAlias(Identifier id) {
        return ALIASED_IDENTIFIERS.containsKey(id);
    }

    public static boolean namespaceHasAlias(Identifier id) {
        return namespaceHasAlias(id.getNamespace());
    }

    public static boolean namespaceHasAlias(String namespace) {
        return ALIASED_NAMESPACES.containsKey(namespace);
    }

    public static boolean pathHasAlias(Identifier id) {
        return pathHasAlias(id.getPath());
    }

    public static boolean pathHasAlias(String path) {
        return ALIASED_PATHS.containsKey(path);
    }

    public static boolean hasAlias(Identifier id) {
        return identifierHasAlias(id)
            || (namespaceHasAlias(id) || pathHasAlias(id));
    }

    public static Identifier resolveNamespaceAlias(Identifier id) {
        if (!namespaceHasAlias(id)) {
            throw NO_NAMESPACE_ALIAS;
        } else {
            return new Identifier(ALIASED_NAMESPACES.get(id.getNamespace()), id.getPath());
        }
    }

    public static Identifier resolvePathAlias(Identifier id) {
        if (!pathHasAlias(id)) {
            throw NO_PATH_ALIAS;
        } else {
            return new Identifier(id.getNamespace(), ALIASED_PATHS.get(id.getPath()));
        }
    }

    public static Identifier resolveAlias(Identifier id) {
        return resolveAlias(id, null);
    }

    public static Identifier resolveAlias(Identifier id, @Nullable Priority specifiedPriority) {

        Identifier aliasedId = new Identifier(id.getNamespace(), id.getPath());
        List<Priority> priorities = Arrays.stream(Priority.values())
            .sorted(Enum::compareTo)
            .collect(Collectors.toCollection(LinkedList::new));

        if (specifiedPriority != null) {
            priorities.remove(specifiedPriority);
            aliasedId = resolve(aliasedId, specifiedPriority);
        }

        for (Priority priority : priorities) {
            aliasedId = resolve(aliasedId, priority);
        }

        if (aliasedId.equals(id)) {
            throw NO_ALIAS;
        }

        return aliasedId;

    }

    private static Identifier resolve(Identifier id, Priority priority) {
        return switch (priority) {
            case IDENTIFIER -> identifierHasAlias(id) ? ALIASED_IDENTIFIERS.get(id) : id;
            case NAMESPACE -> namespaceHasAlias(id) ? new Identifier(ALIASED_NAMESPACES.get(id.getNamespace()), id.getPath()) : id;
            case PATH -> pathHasAlias(id) ? new Identifier(id.getNamespace(), ALIASED_PATHS.get(id.getPath())) : id;
        };
    }


}
