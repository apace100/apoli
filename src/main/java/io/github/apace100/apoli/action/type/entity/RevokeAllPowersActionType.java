package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class RevokeAllPowersActionType {

    public static void action(Entity entity, Identifier source) {

        int revokedPowers = PowerHolderComponent.KEY.maybeGet(entity)
            .map(component -> component.removeAllPowersFromSource(source))
            .orElse(0);

        if (revokedPowers > 0) {
            PowerHolderComponent.sync(entity);
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("revoke_all_powers"),
            new SerializableData()
                .add("source", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> action(entity,
                data.get("source")
            )
        );
    }

}
