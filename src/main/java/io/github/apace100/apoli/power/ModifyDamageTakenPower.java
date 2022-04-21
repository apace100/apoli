package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyDamageTakenPower extends ValueModifyingPower {

    private final Predicate<Pair<DamageSource, Float>> condition;
    private final Predicate<Pair<Entity, Entity>> biEntityCondition;

    private Consumer<Entity> attackerAction;
    private Consumer<Entity> selfAction;
    private Consumer<Pair<Entity, Entity>> biEntityAction;

    private Predicate<Entity> applyArmorCondition;
    private Predicate<Entity> damageArmorCondition;

    public ModifyDamageTakenPower(PowerType<?> type, LivingEntity entity, Predicate<Pair<DamageSource, Float>> condition, Predicate<Pair<Entity, Entity>> biEntityCondition) {
        super(type, entity);
        this.condition = condition;
        this.biEntityCondition = biEntityCondition;
    }

    public void setApplyArmorCondition(Predicate<Entity> applyArmorCondition) {
        this.applyArmorCondition = applyArmorCondition;
    }

    public void setDamageArmorCondition(Predicate<Entity> damageArmorCondition) {
        this.damageArmorCondition = damageArmorCondition;
    }

    public boolean modifiesArmorApplicance() {
        return this.applyArmorCondition != null;
    }

    public boolean shouldApplyArmor() {
        return applyArmorCondition != null && applyArmorCondition.test(entity);
    }

    public boolean modifiesArmorDamaging() {
        return this.damageArmorCondition != null;
    }

    public boolean shouldDamageArmor() {
        return damageArmorCondition != null && damageArmorCondition.test(entity);
    }

    public boolean doesApply(DamageSource source, float damageAmount) {
        return source.getAttacker() == null ? this.condition.test(new Pair(source, damageAmount)) && biEntityCondition == null : this.condition.test(new Pair(source, damageAmount)) && (biEntityCondition == null || biEntityCondition.test(new Pair(source.getAttacker(), entity)));
    }

    public void setAttackerAction(Consumer<Entity> attackerAction) {
        this.attackerAction = attackerAction;
    }

    public void setSelfAction(Consumer<Entity> selfAction) {
        this.selfAction = selfAction;
    }

    public void setBiEntityAction(Consumer<Pair<Entity, Entity>> biEntityAction) {
        this.biEntityAction = biEntityAction;
    }

    public void executeActions(Entity attacker) {
        if(selfAction != null) {
            selfAction.accept(entity);
        }
        if(attackerAction != null) {
            attackerAction.accept(attacker);
        }
        if(biEntityAction != null) {
            biEntityAction.accept(new Pair<>(attacker, entity));
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_damage_taken"),
            new SerializableData()
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null)
                .add("self_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("attacker_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("apply_armor_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                .add("damage_armor_condition", ApoliDataTypes.ENTITY_CONDITION, null),
            data ->
                (type, player) -> {
                    ModifyDamageTakenPower power = new ModifyDamageTakenPower(type, player,
                        data.isPresent("damage_condition") ? data.get("damage_condition") : dmg -> true,
                        data.get("bientity_condition"));
                    data.ifPresent("modifier", power::addModifier);
                    data.<List<Modifier>>ifPresent("modifiers",
                        mods -> mods.forEach(power::addModifier)
                    );
                    if(data.isPresent("bientity_action")) {
                        power.setBiEntityAction(data.get("bientity_action"));
                    }
                    if(data.isPresent("self_action")) {
                        power.setSelfAction(data.get("self_action"));
                    }
                    if(data.isPresent("attacker_action")) {
                        power.setAttackerAction(data.get("attacker_action"));
                    }
                    data.ifPresent("apply_armor_condition", power::setApplyArmorCondition);
                    data.ifPresent("damage_armor_condition", power::setDamageArmorCondition);
                    return power;
                })
            .allowCondition();
    }
}
