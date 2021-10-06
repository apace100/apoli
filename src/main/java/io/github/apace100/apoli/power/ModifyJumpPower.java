package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.List;
import java.util.function.Consumer;

public class ModifyJumpPower extends ValueModifyingPower {

    private final Consumer<Entity> entityAction;

    public ModifyJumpPower(PowerType<?> type, LivingEntity entity, Consumer<Entity> entityAction) {
        super(type, entity);
        this.entityAction = entityAction;
    }

    public void executeAction() {
        if(entityAction != null) {
            entityAction.accept(entity);
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_jump"),
            new SerializableData()
                .add("modifier", SerializableDataTypes.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataTypes.ATTRIBUTE_MODIFIERS, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            data ->
                (type, player) -> {
                    ModifyJumpPower power = new ModifyJumpPower(type, player, (ActionFactory<Entity>.Instance)data.get("entity_action"));
                    if(data.isPresent("modifier")) {
                        power.addModifier(data.getModifier("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        ((List<EntityAttributeModifier>)data.get("modifiers")).forEach(power::addModifier);
                    }
                    return power;
                })
            .allowCondition();
    }
}
