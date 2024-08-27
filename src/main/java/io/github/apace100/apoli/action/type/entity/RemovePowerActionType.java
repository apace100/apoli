package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;

public class RemovePowerActionType {

    public static void action(Entity entity, PowerReference power) {

        List<Identifier> sources = PowerHolderComponent.KEY.maybeGet(entity)
            .stream()
            .map(component -> component.getSources(power))
            .flatMap(Collection::stream)
            .toList();

        if (!sources.isEmpty()) {
            PowerHolderComponent.revokeAllPowersFromAllSources(entity, sources, true);
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("remove_power"),
            new SerializableData()
                .add("power", ApoliDataTypes.POWER_REFERENCE),
            (data, entity) -> action(entity,
                data.get("power")
            )
        );
    }

}
