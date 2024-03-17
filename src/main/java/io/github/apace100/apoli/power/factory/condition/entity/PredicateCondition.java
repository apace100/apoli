package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.Optional;

public class PredicateCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        MinecraftServer server = entity.getServer();
        if (server == null) {
            return false;
        }

        LootCondition predicate = server.getLootManager().getElement(LootDataType.PREDICATES, data.get("predicate"));
        if (predicate == null) {
            return false;
        }

        LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder((ServerWorld) entity.getWorld())
            .add(LootContextParameters.ORIGIN, entity.getPos())
            .addOptional(LootContextParameters.THIS_ENTITY, entity)
            .build(LootContextTypes.COMMAND);
        LootContext lootContext = new LootContext.Builder(lootContextParameterSet)
            .build(Optional.empty());

        return predicate.test(lootContext);

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("predicate"),
            new SerializableData()
                .add("predicate", SerializableDataTypes.IDENTIFIER),
            PredicateCondition::condition
        );
    }

}
