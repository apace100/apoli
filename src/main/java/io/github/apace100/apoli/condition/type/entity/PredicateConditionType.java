package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
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

public class PredicateConditionType {

    public static boolean condition(Entity entity, RegistryKey<LootCondition> predicateKey) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }

        LootCondition predicate = serverWorld.getServer().getReloadableRegistries()
            .getRegistryManager()
            .get(RegistryKeys.PREDICATE)
            .getOrThrow(predicateKey);
        LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(serverWorld)
            .add(LootContextParameters.ORIGIN, entity.getPos())
            .addOptional(LootContextParameters.THIS_ENTITY, entity)
            .build(LootContextTypes.COMMAND);

        return predicate.test(new LootContext.Builder(lootContextParameterSet).build(Optional.empty()));

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("predicate"),
            new SerializableData()
                .add("predicate", SerializableDataTypes.PREDICATE),
            (data, entity) -> condition(entity,
                data.get("predicate")
            )
        );
    }

}
