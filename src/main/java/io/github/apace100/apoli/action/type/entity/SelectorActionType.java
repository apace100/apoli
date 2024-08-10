package io.github.apace100.apoli.action.type.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.ArgumentWrapper;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class SelectorActionType {

    public static void action(Entity entity, EntitySelector selector, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<Pair<Entity, Entity>> biEntityCondition) {

        MinecraftServer server = entity.getWorld().getServer();
        if (server == null) {
            return;
        }

        ServerCommandSource source = entity.getCommandSource()
            .withOutput(Apoli.config.executeCommand.showOutput ? entity : CommandOutput.DUMMY)
            .withLevel(Apoli.config.executeCommand.permissionLevel);

        try {
            selector.getEntities(source)
                .stream()
                .map(selected -> new Pair<>(entity, (Entity) selected))
                .filter(biEntityCondition)
                .forEach(biEntityAction);
        }

        catch (CommandSyntaxException ignored) {

        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("selector_action"),
            new SerializableData()
                .add("selector", ApoliDataTypes.ENTITIES_SELECTOR)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            (data, entity) -> action(entity,
                data.<ArgumentWrapper<EntitySelector>>get("selector").argument(),
                data.get("bientity_action"),
                data.getOrElse("bientity_condition", actorAndTarget -> true)
            )
        );
    }

}
