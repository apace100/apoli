package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.IdentifierAlias;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HasCommandTagCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        Set<String> specifiedCommandTags = new HashSet<>();
        Set<String> commandTags = entity.getCommandTags();

        data.ifPresent("command_tag", specifiedCommandTags::add);
        data.ifPresent("command_tags", specifiedCommandTags::addAll);

        return specifiedCommandTags.isEmpty() ? !commandTags.isEmpty()
                                              : !Collections.disjoint(commandTags, specifiedCommandTags);

    }

    public static ConditionFactory<Entity> getFactory() {
        IdentifierAlias.addPathAlias("has_tag", "has_command_tag");
        return new ConditionFactory<>(
            Apoli.identifier("has_command_tag"),
            new SerializableData()
                .add("tag", SerializableDataTypes.STRING, null)
                .addFunctionedDefault("command_tag", SerializableDataTypes.STRING, data -> data.get("tag"))
                .add("tags", SerializableDataTypes.STRINGS, null)
                .addFunctionedDefault("command_tags", SerializableDataTypes.STRINGS, data -> data.get("tags")),
            HasCommandTagCondition::condition
        );
    }

}
