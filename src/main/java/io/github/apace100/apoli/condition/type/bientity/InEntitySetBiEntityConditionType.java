package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.EntitySetPowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

public class InEntitySetBiEntityConditionType extends BiEntityConditionType {

    public static final DataObjectFactory<InEntitySetBiEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("set", ApoliDataTypes.POWER_REFERENCE),
        data -> new InEntitySetBiEntityConditionType(
            data.get("set")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("set", conditionType.set)
    );

    private final PowerReference set;

    public InEntitySetBiEntityConditionType(PowerReference set) {
        this.set = set;
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BiEntityConditionTypes.IN_ENTITY_SET;
    }

    @Override
    public boolean test(Entity actor, Entity target) {
        return condition(actor, target, set);
    }

    public static boolean condition(Entity actor, Entity target, PowerReference power) {
        return power.getType(actor) instanceof EntitySetPowerType entitySet
            && entitySet.contains(target);
    }

    public static ConditionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("in_entity_set"),
            new SerializableData()
                .add("set", ApoliDataTypes.POWER_REFERENCE),
            (data, actorAndTarget) -> condition(actorAndTarget.getLeft(), actorAndTarget.getRight(),
                data.get("set")
            )
        );
    }

}
