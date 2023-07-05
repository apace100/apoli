package io.github.apace100.apoli.util;

import net.minecraft.advancement.Advancement;
import net.minecraft.server.command.AdvancementCommand;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Set;

public class AdvancementUtil {

    public static void processCriteria(Advancement advancement, Set<String> criteria, AdvancementCommand.Operation operation, ServerPlayerEntity serverPlayerEntity) {
        for (String criterion : criteria.stream().filter(c -> advancement.getCriteria().containsKey(c)).toList()) {
            operation.processEachCriterion(serverPlayerEntity, advancement, criterion);
        }
    }

    public static void processAdvancements(Collection<Advancement> advancements, AdvancementCommand.Operation operation, ServerPlayerEntity serverPlayerEntity) {
        for (Advancement advancement : advancements) {
            operation.processEach(serverPlayerEntity, advancement);
        }
    }

}
