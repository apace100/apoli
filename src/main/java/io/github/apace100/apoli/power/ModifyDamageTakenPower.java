package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
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

    public ModifyDamageTakenPower(PowerType<?> type, LivingEntity entity, Predicate<Pair<DamageSource, Float>> condition, Predicate<Pair<Entity, Entity>> biEntityCondition) {
        super(type, entity);
        this.condition = condition;
        this.biEntityCondition = biEntityCondition;
    }

    public boolean doesApply(DamageSource source, float damageAmount) {
        return source.getAttacker() == null ? this.condition.test(new Pair(source, damageAmount)) && biEntityCondition == null : this.condition.test(new Pair(source, damageAmount)) && (biEntityCondition == null || biEntityCondition.test(new Pair(entity, source.getAttacker())));
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
            biEntityAction.accept(new Pair<>(entity, attacker));
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_damage_taken"),
            new SerializableData()
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("modifier", SerializableDataTypes.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataTypes.ATTRIBUTE_MODIFIERS, null)
                .add("self_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("attacker_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null),
            data ->
                (type, player) -> {
                    ModifyDamageTakenPower power = new ModifyDamageTakenPower(type, player,
                        data.isPresent("damage_condition") ? (ConditionFactory<Pair<DamageSource, Float>>.Instance)data.get("damage_condition") : dmg -> true,
                        (ConditionFactory<Pair<Entity, Entity>>.Instance)data.get("bientity_condition"));
                    if(data.isPresent("modifier")) {
                        power.addModifier(data.getModifier("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        ((List<EntityAttributeModifier>)data.get("modifiers")).forEach(power::addModifier);
                    }
                    if(data.isPresent("bientity_action")) {
                        power.setBiEntityAction((ActionFactory<Pair<Entity, Entity>>.Instance)data.get("bientity_action"));
                    }
                    if(data.isPresent("self_action")) {
                        power.setSelfAction((ActionFactory<Entity>.Instance)data.get("self_action"));
                    }
                    if(data.isPresent("attacker_action")) {
                        power.setAttackerAction((ActionFactory<Entity>.Instance)data.get("attacker_action"));
                    }
                    return power;
                })
            .allowCondition();
    }
}
