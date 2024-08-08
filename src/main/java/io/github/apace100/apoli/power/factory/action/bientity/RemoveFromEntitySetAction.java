package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.action.BiEntityActions;
import io.github.apace100.apoli.power.type.EntitySetPowerType;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

public class RemoveFromEntitySetAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

        PowerHolderComponent component = PowerHolderComponent.KEY.getNullable(actorAndTarget.getLeft());
        Power power = data.get("set");

        if (component == null || power == null || !(component.getPowerType(power) instanceof EntitySetPowerType entitySetPower)) {
            return;
        }

        if (entitySetPower.remove(actorAndTarget.getRight())) {
            PowerHolderComponent.syncPower(actorAndTarget.getLeft(), power);
        }

    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {

        ActionFactory<Pair<Entity, Entity>> factory = new ActionFactory<>(
            Apoli.identifier("remove_from_entity_set"),
            new SerializableData()
                .add("set", ApoliDataTypes.POWER_REFERENCE),
            RemoveFromEntitySetAction::action
        );

        BiEntityActions.ALIASES.addPathAlias("remove_from_set", factory.getSerializerId().getPath());
        return factory;

    }

}
