package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class GrantAdvancementAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity instanceof ServerPlayerEntity player) {
            Identifier id = data.getId("advancement");
            if (player.getServer() != null) {
                Advancement adv = player.getServer().getAdvancementLoader().get(id);
                grant(player, adv);
            }
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("grant_advancement"),
                new SerializableData()
                        .add("advancement", SerializableDataTypes.IDENTIFIER),
                GrantAdvancementAction::action
        );
    }

    private static void grant(ServerPlayerEntity player, Advancement advancement) {
        AdvancementProgress advancementProgress = player.getAdvancementTracker().getProgress(advancement);
        if (!advancementProgress.isDone()) {
            for (String criterion : advancementProgress.getUnobtainedCriteria()) {
                player.getAdvancementTracker().grantCriterion(advancement, criterion);
            }
        }
    }
}
