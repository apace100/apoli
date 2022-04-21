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

public class ModifyDamageDealtPower extends ValueModifyingPower {

    private final Predicate<Pair<DamageSource, Float>> condition;
    private final Predicate<Entity> targetCondition;
    private final Predicate<Pair<Entity, Entity>> biEntityCondition;

    private Consumer<Entity> targetAction;
    private Consumer<Entity> selfAction;
    private Consumer<Pair<Entity, Entity>> biEntityAction;

    public ModifyDamageDealtPower(PowerType<?> type, LivingEntity entity, Predicate<Pair<DamageSource, Float>> condition, Predicate<Entity> targetCondition, Predicate<Pair<Entity, Entity>> biEntityCondition) {
        super(type, entity);
        this.condition = condition;
        this.targetCondition = targetCondition;
        this.biEntityCondition = biEntityCondition;
    }

    public boolean doesApply(DamageSource source, float damageAmount, LivingEntity target) {
        return condition.test(new Pair<>(source, damageAmount)) && (target == null || targetCondition == null || targetCondition.test(target)) && (target == null || biEntityCondition == null || biEntityCondition.test(new Pair<>(entity, target)));
    }

    public void setTargetAction(Consumer<Entity> targetAction) {
        this.targetAction = targetAction;
    }

    public void setBiEntityAction(Consumer<Pair<Entity, Entity>> biEntityAction) {
        this.biEntityAction = biEntityAction;
    }

    public void setSelfAction(Consumer<Entity> selfAction) {
        this.selfAction = selfAction;
    }

    public void executeActions(Entity target) {
        if(selfAction != null) {
            selfAction.accept(entity);
        }
        if(targetAction != null) {
            targetAction.accept(target);
        }
        if(biEntityAction != null) {
            biEntityAction.accept(new Pair<>(entity, target));
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_damage_dealt"),
            new SerializableData()
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null)
                .add("target_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("self_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("target_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null),
            data ->
                (type, player) -> {
                    ModifyDamageDealtPower power = new ModifyDamageDealtPower(type, player,
                        data.isPresent("damage_condition") ? data.get("damage_condition") : dmg -> true,
                        data.get("target_condition"),
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
                    if(data.isPresent("target_action")) {
                        power.setTargetAction(data.get("target_action"));
                    }
                    return power;
                })
            .allowCondition();
    }
}
