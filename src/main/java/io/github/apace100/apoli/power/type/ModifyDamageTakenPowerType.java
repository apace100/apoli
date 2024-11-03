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
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ModifyDamageTakenPowerType extends ValueModifyingPowerType {

    public static final TypedDataObjectFactory<ModifyDamageTakenPowerType> DATA_FACTORY = createConditionedModifyingDataFactory(
        new SerializableData()
            .add("self_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("attacker_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("apply_armor_condition", EntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("damage_armor_condition", EntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("damage_condition", DamageCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, modifiers, condition) -> new ModifyDamageTakenPowerType(
            data.get("self_action"),
            data.get("attacker_action"),
            data.get("bientity_action"),
            data.get("apply_armor_condition"),
            data.get("damage_armor_condition"),
            data.get("bientity_condition"),
            data.get("damage_condition"),
            modifiers,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("self_action", powerType.selfAction)
            .set("attacker_action", powerType.attackerAction)
            .set("bientity_action", powerType.biEntityAction)
            .set("apply_armor_condition", powerType.applyArmorCondition)
            .set("damage_armor_condition", powerType.damageArmorCondition)
            .set("bientity_condition", powerType.biEntityCondition)
            .set("damage_condition", powerType.damageCondition)
    );

    private final Optional<EntityAction> selfAction;
    private final Optional<EntityAction> attackerAction;
    private final Optional<BiEntityAction> biEntityAction;

    private final Optional<EntityCondition> applyArmorCondition;
    private final Optional<EntityCondition> damageArmorCondition;
    private final Optional<BiEntityCondition> biEntityCondition;
    private final Optional<DamageCondition> damageCondition;

    public ModifyDamageTakenPowerType(Optional<EntityAction> selfAction, Optional<EntityAction> attackerAction, Optional<BiEntityAction> biEntityAction, Optional<EntityCondition> applyArmorCondition, Optional<EntityCondition> damageArmorCondition, Optional<BiEntityCondition> biEntityCondition, Optional<DamageCondition> damageCondition, List<Modifier> modifiers, Optional<EntityCondition> condition) {
        super(modifiers, condition);

        this.selfAction = selfAction;
        this.attackerAction = attackerAction;
        this.biEntityAction = biEntityAction;
        this.applyArmorCondition = applyArmorCondition;
        this.damageArmorCondition = damageArmorCondition;
        this.biEntityCondition = biEntityCondition;
        this.damageCondition = damageCondition;

    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_DAMAGE_TAKEN;
    }

    public boolean modifiesArmorApplicance() {
        return this.applyArmorCondition.isPresent();
    }

    public boolean shouldApplyArmor() {
        return applyArmorCondition
            .map(condition -> condition.test(getHolder()))
            .orElse(false);
    }

    public boolean modifiesArmorDamaging() {
        return this.damageArmorCondition.isPresent();
    }

    public boolean shouldDamageArmor() {
        return damageArmorCondition
            .map(condition -> condition.test(getHolder()))
            .orElse(false);
    }

    public boolean doesApply(DamageSource source, float damageAmount) {
        Entity attacker = source.getAttacker();
        return attacker == null
            ? damageCondition.map(condition -> condition.test(source, damageAmount)).orElse(true)
                && biEntityCondition.isEmpty()
            : damageCondition.map(condition -> condition.test(source, damageAmount)).orElse(true)
                && biEntityCondition.map(condition -> condition.test(attacker, getHolder())).orElse(true);
    }

    public void executeActions(Entity attacker) {

        selfAction.ifPresent(action -> action.execute(getHolder()));
        attackerAction.ifPresent(action -> action.execute(getHolder()));

        biEntityAction.ifPresent(action -> action.execute(attacker, getHolder()));

    }

}
