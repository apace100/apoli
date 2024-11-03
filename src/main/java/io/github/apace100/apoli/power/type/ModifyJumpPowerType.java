package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ModifyJumpPowerType extends ValueModifyingPowerType {

    public static final TypedDataObjectFactory<ModifyJumpPowerType> DATA_FACTORY = createConditionedModifyingDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty()),
        (data, modifiers, condition) -> new ModifyJumpPowerType(
            data.get("entity_action"),
            modifiers,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
    );

    private final Optional<EntityAction> entityAction;

    public ModifyJumpPowerType(Optional<EntityAction> entityAction, List<Modifier> modifiers, Optional<EntityCondition> condition) {
        super(modifiers, condition);
        this.entityAction = entityAction;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_JUMP;
    }

    public void executeAction() {
        entityAction.ifPresent(action -> action.execute(getHolder()));
    }

}
