package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
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

public class PredicateEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<PredicateEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("predicate", SerializableDataTypes.PREDICATE),
        data -> new PredicateEntityConditionType(
            data.get("predicate")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("predicate", conditionType.predicate)
    );

    private final RegistryKey<LootCondition> predicate;

    public PredicateEntityConditionType(RegistryKey<LootCondition> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(Entity entity) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }

        LootCondition lootCondition = serverWorld.getServer().getReloadableRegistries()
            .getRegistryManager()
            .get(RegistryKeys.PREDICATE)
            .getOrThrow(predicate);
        LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(serverWorld)
            .add(LootContextParameters.ORIGIN, entity.getPos())
            .addOptional(LootContextParameters.THIS_ENTITY, entity)
            .build(LootContextTypes.COMMAND);

        return lootCondition.test(new LootContext.Builder(lootContextParameterSet).build(Optional.empty()));

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.PREDICATE;
    }

}
