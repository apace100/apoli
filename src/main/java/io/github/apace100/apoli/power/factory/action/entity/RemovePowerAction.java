package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.util.List;

public class RemovePowerAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        PowerHolderComponent.KEY.maybeGet(entity).ifPresent(
            component -> {
                PowerType power = data.get("power");
                List<Identifier> sources = component.getSources(power);
                for (Identifier source : sources) {
                    component.removePower(power, source);
                }
                if (sources.size() > 0) {
                    component.sync();
                }
            }
        );
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("remove_power"),
            new SerializableData()
                .add("power", ApoliDataTypes.POWER_TYPE),
            RemovePowerAction::action
        );
    }
}
