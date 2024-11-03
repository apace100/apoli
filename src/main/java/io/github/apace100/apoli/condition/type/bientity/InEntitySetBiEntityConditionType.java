package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.EntitySetPowerType;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class InEntitySetBiEntityConditionType extends BiEntityConditionType {

    public static final TypedDataObjectFactory<InEntitySetBiEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
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
    public @NotNull ConditionConfiguration<?> getConfig() {
        return BiEntityConditionTypes.IN_ENTITY_SET;
    }

    @Override
    public boolean test(Entity actor, Entity target) {
        return set.getPowerTypeFrom(actor) instanceof EntitySetPowerType entitySet
            && entitySet.contains(target);
    }

}
