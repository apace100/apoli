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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyDamageDealtPowerType extends ValueModifyingPowerType {

    private final Consumer<Entity> selfAction;
    private final Consumer<Entity> targetAction;
    private final Consumer<Pair<Entity, Entity>> biEntityAction;

    private final Predicate<Entity> targetCondition;
    private final Predicate<Pair<Entity, Entity>> biEntityCondition;
    private final Predicate<Pair<DamageSource, Float>> damageCondition;


    public ModifyDamageDealtPowerType(Power power, LivingEntity entity, Consumer<Entity> selfAction, Consumer<Entity> targetAction, Consumer<Pair<Entity, Entity>> biEntityAction, Predicate<Entity> targetCondition, Predicate<Pair<Entity, Entity>> biEntityCondition, Predicate<Pair<DamageSource, Float>> damageCondition, Modifier modifier, List<Modifier> modifiers) {
        super(power, entity);

        this.selfAction = selfAction;
        this.targetAction = targetAction;
        this.biEntityAction = biEntityAction;

        this.targetCondition = targetCondition;
        this.biEntityCondition = biEntityCondition;
        this.damageCondition = damageCondition;

        if (modifier != null) {
            this.addModifier(modifier);
        }

        if (modifiers != null) {
            modifiers.forEach(this::addModifier);
        }

    }

    public boolean doesApply(DamageSource source, float damageAmount, @Nullable LivingEntity target) {
        return (damageCondition == null || damageCondition.test(new Pair<>(source, damageAmount)))
            && (target == null || targetCondition == null || targetCondition.test(target))
            && (target == null || biEntityCondition == null || biEntityCondition.test(new Pair<>(entity, target)));
    }

    public void executeActions(Entity target) {

        if (selfAction != null) {
            selfAction.accept(entity);
        }

        if (targetAction != null) {
            targetAction.accept(target);
        }

        if (biEntityAction != null) {
            biEntityAction.accept(new Pair<>(entity, target));
        }

    }

    public static PowerTypeFactory<ModifyDamageDealtPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_damage_dealt"),
            new SerializableData()
                .add("self_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("target_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
                .add("target_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data -> (power, entity) -> new ModifyDamageDealtPowerType(power, entity,
                data.get("self_action"),
                data.get("target_action"),
                data.get("bientity_action"),
                data.get("target_condition"),
                data.get("bientity_condition"),
                data.get("damage_condition"),
                data.get("modifier"),
                data.get("modifiers")
            )
        ).allowCondition();
    }

}
