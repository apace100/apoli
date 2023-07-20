package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.ResourceOperation;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class ModifyResourceAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if(entity instanceof LivingEntity living) {
            PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
            PowerType<?> powerType = data.get("resource");
            Power p = component.getPower(powerType);
            Modifier modifier = data.get("modifier");
            if(p instanceof VariableIntPower vip) {
                vip.setValue((int)modifier.apply(entity, vip.getValue()));
                PowerHolderComponent.syncPower(entity, powerType);
            } else if(p instanceof CooldownPower cp) {
                int targetRemainingTicks = (int)modifier.apply(entity, cp.getRemainingTicks());
                if(targetRemainingTicks < 0) {
                    targetRemainingTicks = 0;
                }
                cp.modify(targetRemainingTicks - cp.getRemainingTicks());
                PowerHolderComponent.syncPower(entity, powerType);
            }
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("modify_resource"),
            new SerializableData()
                .add("modifier", Modifier.DATA_TYPE)
                .add("resource", ApoliDataTypes.POWER_TYPE),
            ModifyResourceAction::action
        );
    }
}
