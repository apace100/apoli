package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class DamageConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        MetaConditions.register(ApoliDataTypes.DAMAGE_CONDITION, DamageConditions::register);
        register(new ConditionFactory<>(Apoli.identifier("amount"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, dmg) -> ((Comparison)data.get("comparison")).compare(dmg.getRight(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("fire"), new SerializableData(),
            (data, dmg) -> dmg.getLeft().isIn(DamageTypeTags.IS_FIRE)));
        register(new ConditionFactory<>(Apoli.identifier("name"), new SerializableData()
            .add("name", SerializableDataTypes.STRING),
            (data, dmg) -> dmg.getLeft().getName().equals(data.getString("name"))));
        register(new ConditionFactory<>(Apoli.identifier("projectile"), new SerializableData()
            .add("projectile", SerializableDataTypes.ENTITY_TYPE, null)
            .add("projectile_condition", ApoliDataTypes.ENTITY_CONDITION, null),
            (data, dmg) -> {
                if(dmg.getLeft().isIn(DamageTypeTags.IS_PROJECTILE)) {
                    Entity projectile = dmg.getLeft().getSource();
                    if(projectile != null) {
                        if(data.isPresent("projectile") && projectile.getType() != data.get("projectile")) {
                            return false;
                        }
                        Predicate<Entity> projectileCondition = data.get("projectile_condition");
                        return projectileCondition == null || projectileCondition.test(projectile);
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("attacker"), new SerializableData()
            .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null),
            (data, dmg) -> {
                Entity attacker = dmg.getLeft().getAttacker();
                if(attacker instanceof LivingEntity) {
                    if(!data.isPresent("entity_condition") || ((ConditionFactory<LivingEntity>.Instance)data.get("entity_condition")).test((LivingEntity)attacker)) {
                        return true;
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("bypasses_armor"), new SerializableData(),
            (data, dmg) -> dmg.getLeft().isIn(DamageTypeTags.BYPASSES_ARMOR)));
        register(new ConditionFactory<>(Apoli.identifier("explosive"), new SerializableData(),
            (data, dmg) -> dmg.getLeft().isIn(DamageTypeTags.IS_EXPLOSION)));
        register(new ConditionFactory<>(Apoli.identifier("from_falling"), new SerializableData(),
            (data, dmg) -> dmg.getLeft().isIn(DamageTypeTags.IS_FALL)));
        register(new ConditionFactory<>(Apoli.identifier("unblockable"), new SerializableData(),
            (data, dmg) -> dmg.getLeft().isIn(DamageTypeTags.BYPASSES_SHIELD)));
        register(new ConditionFactory<>(Apoli.identifier("out_of_world"), new SerializableData(),
            (data, dmg) -> dmg.getLeft().isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)));
        register(new ConditionFactory<>(Apoli.identifier("in_tag"), new SerializableData()
                .add("tag", SerializableDataType.tag(RegistryKeys.DAMAGE_TYPE)),
                (data, dmg) -> dmg.getLeft().isIn(data.get("tag"))));
    }

    private static void register(ConditionFactory<Pair<DamageSource, Float>> conditionFactory) {
        Registry.register(ApoliRegistries.DAMAGE_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
