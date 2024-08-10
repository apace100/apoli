package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HasCommandTagConditionType {

    public static boolean condition(Entity entity, Collection<String> specifiedCommandTags) {
        Set<String> commandTags = entity.getCommandTags();
        return specifiedCommandTags.isEmpty()
            ? !commandTags.isEmpty()
            : !Collections.disjoint(commandTags, specifiedCommandTags);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("has_command_tag"),
            new SerializableData()
                .add("command_tag", SerializableDataTypes.STRING, null)
                .add("command_tags", SerializableDataTypes.STRINGS, null),
            (data, entity) -> {

                Collection<String> commandTags = new HashSet<>();

                data.ifPresent("command_tag", commandTags::add);
                data.ifPresent("command_tags", commandTags::addAll);

                return condition(entity, commandTags);

            }
        );
    }

}
