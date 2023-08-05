package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.AdvancementCommandAccessor;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.AdvancementUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.command.AdvancementCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class GrantAdvancementAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        MinecraftServer server = entity.getServer();
        if (server == null || !(entity instanceof ServerPlayerEntity serverPlayerEntity)) {
            return;
        }

        ServerAdvancementLoader advancementLoader = server.getAdvancementLoader();
        AdvancementCommand.Selection selection = data.get("selection");

        if (selection == AdvancementCommand.Selection.EVERYTHING) {
            AdvancementUtil.processAdvancements(advancementLoader.getAdvancements(), AdvancementCommand.Operation.GRANT, serverPlayerEntity);
        } else if (data.isPresent("advancement")) {

            Identifier advancementId = data.get("advancement");
            Advancement advancement = advancementLoader.get(advancementId);
            if (advancement == null) {
                Apoli.LOGGER.warn("Unknown advancement (\"" + advancementId + "\") referenced in `grant_advancement` entity action type!");
                return;
            }

            Set<String> criteria = new HashSet<>();

            data.ifPresent("criterion", criteria::add);
            data.ifPresent("criteria", criteria::addAll);

            if (criteria.isEmpty()) {
                AdvancementUtil.processAdvancements(AdvancementCommandAccessor.callSelect(advancement, selection), AdvancementCommand.Operation.GRANT, serverPlayerEntity);
            } else {
                AdvancementUtil.processCriteria(advancement, criteria, AdvancementCommand.Operation.GRANT, serverPlayerEntity);
            }

        }

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("grant_advancement"),
            new SerializableData()
                .add("advancement", SerializableDataTypes.IDENTIFIER, null)
                .add("criterion", SerializableDataTypes.STRING, null)
                .add("criteria", SerializableDataTypes.STRINGS, null)
                .add("selection", ApoliDataTypes.ADVANCEMENT_SELECTION, AdvancementCommand.Selection.ONLY),
            GrantAdvancementAction::action
        );
    }

}
