package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.DamageCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class PreventDeathPowerType extends PowerType {

    public static final TypedDataObjectFactory<PreventDeathPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("damage_condition", DamageCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, condition) -> new PreventDeathPowerType(
            data.get("entity_action"),
            data.get("damage_condition"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
            .set("damage_condition", powerType.damageCondition)
    );

    private final Optional<EntityAction> entityAction;
    private final Optional<DamageCondition> damageCondition;

    public PreventDeathPowerType(Optional<EntityAction> entityAction, Optional<DamageCondition> damageCondition, Optional<EntityCondition> condition) {
        super(condition);
        this.entityAction = entityAction;
        this.damageCondition = damageCondition;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_DEATH;
    }

    public boolean doesApply(DamageSource source, float amount) {
        return damageCondition
            .map(condition -> condition.test(source, amount))
            .orElse(true);
    }

    public void executeAction() {
        entityAction.ifPresent(action -> action.execute(getHolder()));
    }

    public static boolean doesPrevent(Entity holder, DamageSource source, float amount) {

        if (!(holder instanceof LivingEntity livingHolder)) {
            return false;
        }

        List<PreventDeathPowerType> types = PowerHolderComponent.getPowerTypes(livingHolder, PreventDeathPowerType.class);
        boolean prevented = false;

        for (var type : types) {

            if (type.doesApply(source, amount)) {

                if (!prevented) {
                    livingHolder.setHealth(1.0F);
                }

                type.executeAction();
                prevented = true;

            }

        }

        return prevented;

    }

}
