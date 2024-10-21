package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.type.DamageConditionType;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;

import java.util.Optional;

public class ProjectileDamageConditionType extends DamageConditionType {

    public static final TypedDataObjectFactory<ProjectileDamageConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("projectile", SerializableDataTypes.ENTITY_TYPE.optional(), Optional.empty())
            .add("projectile_condition", EntityCondition.DATA_TYPE.optional(), Optional.empty()),
        data -> new ProjectileDamageConditionType(
            data.get("projectile"),
            data.get("projectile_condition")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("projectile", conditionType.projectile)
            .set("projectile_condition", conditionType.projectileCondition)
    );

    private final Optional<EntityType<?>> projectile;
    private final Optional<EntityCondition> projectileCondition;

    public ProjectileDamageConditionType(Optional<EntityType<?>> projectile, Optional<EntityCondition> projectileCondition) {
        this.projectile = projectile;
        this.projectileCondition = projectileCondition;
    }

    @Override
    public boolean test(DamageSource source, float amount) {
        Entity entitySource = source.getSource();
        return source.isIn(DamageTypeTags.IS_PROJECTILE)
            && entitySource != null
            && projectile.map(entitySource.getType()::equals).orElse(true)
            && projectileCondition.map(entityCondition -> entityCondition.test(entitySource)).orElse(true);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return DamageConditionTypes.PROJECTILE;
    }

}
