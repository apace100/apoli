package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class PowerEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<PowerEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("power", ApoliDataTypes.POWER_REFERENCE)
            .add("source", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty()),
        data -> new PowerEntityConditionType(
            data.get("power"),
            data.get("source")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("power", conditionType.power)
            .set("source", conditionType.source)
    );

    private final PowerReference power;
    private final Optional<Identifier> source;

    public PowerEntityConditionType(PowerReference power, Optional<Identifier> source) {
        this.power = power;
        this.source = source;
    }

    @Override
    public boolean test(Entity entity) {
        return PowerHolderComponent.getOptional(entity)
            .map(component -> source
                .map(id -> component.hasPower(power, id))
                .orElseGet(() -> component.hasPower(power)))
            .orElse(false);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.POWER;
    }

}
