package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class ProjectileConditionType {

    public static boolean condition(DamageSource damageSource, EntityType<?> projectileType, Predicate<Entity> projectileCondition) {

        Entity source = damageSource.getSource();

        return damageSource.isIn(DamageTypeTags.IS_PROJECTILE)
            && source != null
            && (projectileType == null || source.getType() == projectileType)
            && projectileCondition.test(source);

    }

    public static ConditionTypeFactory<Pair<DamageSource, Float>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("projectile"),
            new SerializableData()
                .add("projectile", SerializableDataTypes.ENTITY_TYPE, null)
                .add("projectile_condition", ApoliDataTypes.ENTITY_CONDITION, null),
            (data, sourceAndAmount) -> condition(sourceAndAmount.getLeft(),
                data.get("projectile"),
                data.getOrElse("projectile_condition", e -> true)
            )
        );
    }

}
