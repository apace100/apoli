package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.DamageCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ModifyDamageDealtPowerType extends ValueModifyingPowerType {

    public static final TypedDataObjectFactory<ModifyDamageDealtPowerType> DATA_FACTORY = createConditionedModifyingDataFactory(
        new SerializableData()
            .add("self_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("target_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("target_condition", EntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("damage_condition", DamageCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, modifiers, condition) -> new ModifyDamageDealtPowerType(
            data.get("self_action"),
            data.get("target_action"),
            data.get("bientity_action"),
            data.get("target_condition"),
            data.get("bientity_condition"),
            data.get("damage_condition"),
            modifiers,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("self_action", powerType.selfAction)
            .set("target_action", powerType.targetAction)
            .set("bientity_action", powerType.biEntityAction)
            .set("target_condition", powerType.targetCondition)
            .set("bientity_condition", powerType.biEntityCondition)
            .set("damage_condition", powerType.damageCondition)
    );

    private final Optional<EntityAction> selfAction;
    private final Optional<EntityAction> targetAction;
    private final Optional<BiEntityAction> biEntityAction;

    private final Optional<EntityCondition> targetCondition;
    private final Optional<BiEntityCondition> biEntityCondition;
    private final Optional<DamageCondition> damageCondition;

    public ModifyDamageDealtPowerType(Optional<EntityAction> selfAction, Optional<EntityAction> targetAction, Optional<BiEntityAction> biEntityAction, Optional<EntityCondition> targetCondition, Optional<BiEntityCondition> biEntityCondition, Optional<DamageCondition> damageCondition, List<Modifier> modifiers, Optional<EntityCondition> condition) {
        super(modifiers, condition);
        this.selfAction = selfAction;
        this.targetAction = targetAction;
        this.biEntityAction = biEntityAction;
        this.targetCondition = targetCondition;
        this.biEntityCondition = biEntityCondition;
        this.damageCondition = damageCondition;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_DAMAGE_DEALT;
    }

    public boolean doesApply(DamageSource source, float damageAmount, @Nullable LivingEntity target) {
        return damageCondition.map(condition -> condition.test(source, damageAmount)).orElse(true)
            && (target == null || targetCondition.map(condition -> condition.test(target)).orElse(true))
            && (target == null || biEntityCondition.map(condition -> condition.test(getHolder(), target)).orElse(true));
    }

    public void executeActions(Entity target) {

        selfAction.ifPresent(action -> action.execute(getHolder()));
        targetAction.ifPresent(action -> action.execute(target));

        biEntityAction.ifPresent(action -> action.execute(getHolder(), target));

    }

}
