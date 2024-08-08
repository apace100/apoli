package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.List;
import java.util.function.Consumer;

public class ModifyJumpPowerType extends ValueModifyingPowerType {

    private final Consumer<Entity> entityAction;

    public ModifyJumpPowerType(Power power, LivingEntity entity, Consumer<Entity> entityAction, Modifier modifier, List<Modifier> modifiers) {
        super(power, entity);
        this.entityAction = entityAction;

        if (modifier != null) {
            this.addModifier(modifier);
        }

        if (modifiers != null) {
            modifiers.forEach(this::addModifier);
        }

    }

    public void executeAction() {

        if (entityAction != null) {
            entityAction.accept(entity);
        }

    }

    public static PowerTypeFactory<ModifyJumpPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_jump"),
            new SerializableData()
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            data -> (power, entity) -> new ModifyJumpPowerType(power, entity,
                data.get("entity_action"),
                data.get("modifier"),
                data.get("modifiers")
            )
        ).allowCondition();
    }
}
