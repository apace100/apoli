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

public class ModifyProjectileDamagePower extends ValueModifyingPower {

    private final Predicate<Pair<DamageSource, Float>> condition;
    private final Predicate<Entity> targetCondition;

    private Consumer<Entity> targetAction;
    private Consumer<Entity> selfAction;

    public ModifyProjectileDamagePower(PowerType<?> type, LivingEntity entity, Predicate<Pair<DamageSource, Float>> condition, Predicate<Entity> targetCondition) {
        super(type, entity);
        this.condition = condition;
        this.targetCondition = targetCondition;
    }

    public boolean doesApply(DamageSource source, float damageAmount, LivingEntity target) {
        return condition.test(new Pair<>(source, damageAmount)) && (target == null || targetCondition == null || targetCondition.test(target));
    }

    public void setTargetAction(Consumer<Entity> targetAction) {
        this.targetAction = targetAction;
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
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_projectile_damage"),
            new SerializableData()
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null)
                .add("target_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                .add("self_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("target_action", ApoliDataTypes.ENTITY_ACTION, null),
            data ->
                (type, player) -> {
                    ModifyProjectileDamagePower power = new ModifyProjectileDamagePower(type, player,
                        data.isPresent("damage_condition") ? data.get("damage_condition") : dmg -> true,
                        data.get("target_condition"));
                    data.ifPresent("modifier", power::addModifier);
                    data.<List<Modifier>>ifPresent("modifiers",
                        mods -> mods.forEach(power::addModifier)
                    );
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
