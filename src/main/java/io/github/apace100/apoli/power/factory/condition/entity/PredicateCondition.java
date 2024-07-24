package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;

import java.util.Optional;

public class PredicateCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }

        RegistryKey<LootCondition> predicateKey = data.get("predicate");
        LootCondition predicate = serverWorld.getServer().getReloadableRegistries()
            .getRegistryManager()
            .get(RegistryKeys.PREDICATE)
            .getOrThrow(predicateKey);

        LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(serverWorld)
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
                .add("predicate", SerializableDataTypes.PREDICATE),
            PredicateCondition::condition
        );
    }

}
