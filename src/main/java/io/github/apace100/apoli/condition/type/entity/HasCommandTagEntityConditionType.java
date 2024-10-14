package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class HasCommandTagEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<HasCommandTagEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("command_tag", SerializableDataTypes.STRING.optional(), Optional.empty())
            .add("command_tags", SerializableDataTypes.STRINGS.optional(), Optional.empty()),
        data -> new HasCommandTagEntityConditionType(
            data.get("command_tag"),
            data.get("command_tags")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("command_tag", conditionType.commandTag)
            .set("command_tags", conditionType.commandTags)
    );

    private final Optional<String> commandTag;
    private final Optional<List<String>> commandTags;

    private final Set<String> specifiedCommandTags;

    public HasCommandTagEntityConditionType(Optional<String> commandTag, Optional<List<String>> commandTags) {

        this.commandTag = commandTag;
        this.commandTags = commandTags;

        this.specifiedCommandTags = new ObjectOpenHashSet<>();

        this.commandTag.ifPresent(this.specifiedCommandTags::add);
        this.commandTags.ifPresent(this.specifiedCommandTags::addAll);

    }

    @Override
    public boolean test(Entity entity) {
        Set<String> commandTags = entity.getCommandTags();
        return specifiedCommandTags.isEmpty()
            ? !commandTags.isEmpty()
            : !Collections.disjoint(commandTags, specifiedCommandTags);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.HAS_COMMAND_TAG;
    }

}
