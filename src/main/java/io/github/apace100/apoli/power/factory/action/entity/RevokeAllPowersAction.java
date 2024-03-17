package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class RevokeAllPowersAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity).orElse(null);

        if (component != null) {

            Identifier source = data.get("source");
            component.removeAllPowersFromSource(source);

        }

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("revoke_all_powers"),
            new SerializableData()
                .add("source", SerializableDataTypes.IDENTIFIER),
            RevokeAllPowersAction::action
        );
    }

}
