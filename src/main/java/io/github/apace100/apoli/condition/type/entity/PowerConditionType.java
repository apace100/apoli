package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class PowerConditionType {

    public static boolean condition(Entity entity, PowerReference power, @Nullable Identifier source) {
        return PowerHolderComponent.KEY.maybeGet(entity)
            .map(component -> source != null ? component.hasPower(power, source) : component.hasPower(power))
            .orElse(false);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("power"),
            new SerializableData()
                .add("power", ApoliDataTypes.POWER_REFERENCE)
                .add("source", SerializableDataTypes.IDENTIFIER, null),
            (data, entity) -> condition(entity,
                data.get("power"),
                data.get("source")
            )
        );
    }

}
