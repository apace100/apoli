package io.github.apace100.apoli.action.type.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import io.github.apace100.calio.util.ArgumentWrapper;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;

public class SelectorActionEntityActionType extends EntityActionType {

    public static final DataObjectFactory<SelectorActionEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("selector", ApoliDataTypes.ENTITIES_SELECTOR)
            .add("bientity_action", BiEntityAction.DATA_TYPE)
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty()),
        data -> new SelectorActionEntityActionType(
            data.get("selector"),
            data.get("bientity_action"),
            data.get("bientity_condition")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("selector", actionType.selector)
            .set("bientity_action", actionType.biEntityAction)
            .set("bientity_condition", actionType.biEntityCondition)
    );

    private final ArgumentWrapper<EntitySelector> selector;
    private final EntitySelector unwrappedSelector;

    private final BiEntityAction biEntityAction;
    private final Optional<BiEntityCondition> biEntityCondition;

    public SelectorActionEntityActionType(ArgumentWrapper<EntitySelector> selector, BiEntityAction biEntityAction, Optional<BiEntityCondition> biEntityCondition) {
        this.selector = selector;
        this.unwrappedSelector = selector.argument();
        this.biEntityAction = biEntityAction;
        this.biEntityCondition = biEntityCondition;
    }

    @Override
    protected void execute(Entity entity) {

        MinecraftServer server = entity.getServer();
        if (server == null) {
            return;
        }

        ServerCommandSource commandSource = entity.getCommandSource()
            .withOutput(CommandOutput.DUMMY)
            .withLevel(Apoli.config.executeCommand.permissionLevel);

        if (Apoli.config.executeCommand.showOutput) {
            commandSource = commandSource.withOutput(entity instanceof ServerPlayerEntity serverPlayer && serverPlayer.networkHandler != null
                ? serverPlayer
                : server);
        }

        try {
            unwrappedSelector.getEntities(commandSource)
                .stream()
                .filter(selected -> biEntityCondition.map(condition -> condition.test(entity, selected)).orElse(true))
                .forEach(selected -> biEntityAction.execute(entity, selected));
        }

        catch (CommandSyntaxException cse) {
            commandSource.sendError(Text.of(cse.getRawMessage()));
		}

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.SELECTOR_ACTION;
    }

}
