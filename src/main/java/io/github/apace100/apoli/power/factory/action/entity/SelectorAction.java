package io.github.apace100.apoli.power.factory.action.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.ArgumentWrapper;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class SelectorAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        MinecraftServer server = entity.getWorld().getServer();
        if (server == null) return;

        EntitySelector selector = data.<ArgumentWrapper<EntitySelector>>get("selector").get();
        Predicate<Pair<Entity, Entity>> biEntityCondition = data.get("bientity_condition");
        Consumer<Pair<Entity, Entity>> biEntityAction = data.get("bientity_action");

        ServerCommandSource source = new ServerCommandSource(
            CommandOutput.DUMMY,
            entity.getPos(),
            entity.getRotationClient(),
            (ServerWorld) entity.getWorld(),
            2,
            entity.getNameForScoreboard(),
            entity.getName(),
            server,
            entity
        );

        try {
            selector.getEntities(source)
                .stream()
                .filter(e -> biEntityCondition == null || biEntityCondition.test(new Pair<>(entity, e)))
                .forEach(e -> biEntityAction.accept(new Pair<>(entity, e)));
        }

        catch (CommandSyntaxException ignored) {}

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("selector_action"),
            new SerializableData()
                .add("selector", ApoliDataTypes.ENTITIES_SELECTOR)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            SelectorAction::action
        );
    }

}
