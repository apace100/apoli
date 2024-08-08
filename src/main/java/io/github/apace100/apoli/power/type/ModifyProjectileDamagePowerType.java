package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyProjectileDamagePowerType extends ValueModifyingPowerType {

    private final Consumer<Entity> selfAction;
    private final Consumer<Entity> targetAction;

    private final Predicate<Entity> targetCondition;
    private final Predicate<Pair<DamageSource, Float>> damageCondition;

    public ModifyProjectileDamagePowerType(Power power, LivingEntity entity, Consumer<Entity> selfAction, Consumer<Entity> targetAction, Predicate<Entity> targetCondition, Predicate<Pair<DamageSource, Float>> damageCondition, Modifier modifier, List<Modifier> modifiers) {
        super(power, entity);

        this.selfAction = selfAction;
        this.targetAction = targetAction;
        this.damageCondition = damageCondition;
        this.targetCondition = targetCondition;

        if (modifier != null) {
            this.addModifier(modifier);
        }

        if (modifiers != null) {
            modifiers.forEach(this::addModifier);
        }

    }

    public boolean doesApply(DamageSource source, float damageAmount, LivingEntity target) {
        return damageCondition.test(new Pair<>(source, damageAmount)) && (target == null || targetCondition == null || targetCondition.test(target));
    }

    public void executeActions(Entity target) {

        if (selfAction != null) {
            selfAction.accept(entity);
        }

        if (targetAction != null) {
            targetAction.accept(target);
        }

    }

    public static PowerTypeFactory<ModifyProjectileDamagePowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_projectile_damage"),
            new SerializableData()
                .add("self_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("target_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("target_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data -> (power, entity) -> new ModifyProjectileDamagePowerType(power, entity,
                data.get("self_action"),
                data.get("target_action"),
                data.get("target_condition"),
                data.get("damage_condition"),
                data.get("modifier"),
                data.get("modifiers")
            )
        ).allowCondition();
    }

}
