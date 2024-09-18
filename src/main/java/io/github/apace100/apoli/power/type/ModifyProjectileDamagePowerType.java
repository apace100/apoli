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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyProjectileDamagePowerType extends ValueModifyingPowerType {

    private final Consumer<Entity> selfAction;
    private final Consumer<Entity> targetAction;

    private final Predicate<Entity> targetCondition;
    private final Predicate<Pair<DamageSource, Float>> damageCondition;

    public ModifyProjectileDamagePowerType(Power power, LivingEntity entity, Consumer<Entity> selfAction, Consumer<Entity> targetAction, Predicate<Entity> targetCondition, Predicate<Pair<DamageSource, Float>> damageCondition, Optional<Modifier> modifier, Optional<List<Modifier>> modifiers) {
        super(power, entity);

        this.selfAction = selfAction;
        this.targetAction = targetAction;
        this.targetCondition = targetCondition;
        this.damageCondition = damageCondition;

        modifier.ifPresent(this::addModifier);
        modifiers.ifPresent(mods -> mods.forEach(this::addModifier));

    }

    public boolean doesApply(DamageSource source, float damageAmount, LivingEntity target) {
        return damageCondition.test(new Pair<>(source, damageAmount))
            && (target == null || targetCondition.test(target));
    }

    public void executeActions(Entity target) {
        selfAction.accept(entity);
        targetAction.accept(target);
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_projectile_damage"),
            new SerializableData()
                .add("self_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("target_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("target_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("modifier", Modifier.DATA_TYPE.optional(), Optional.empty())
                .add("modifiers", Modifier.LIST_TYPE.optional(), Optional.empty()),
            data -> (power, entity) -> new ModifyProjectileDamagePowerType(power, entity,
                data.getOrElse("self_action", e -> {}),
                data.getOrElse("target_action", e -> {}),
                data.getOrElse("target_condition", e -> true),
                data.getOrElse("damage_condition", dmg -> true),
                data.get("modifier"),
                data.get("modifiers")
            )
        ).allowCondition();
    }

}
