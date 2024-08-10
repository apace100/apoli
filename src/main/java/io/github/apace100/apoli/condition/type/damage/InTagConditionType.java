package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class InTagConditionType {

    public static boolean condition(DamageSource damageSource, TagKey<DamageType> damageTypeTag) {
        return damageSource.isIn(damageTypeTag);
    }

    public static ConditionTypeFactory<Pair<DamageSource, Float>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("in_tag"),
            new SerializableData()
                .add("tag", SerializableDataType.tag(RegistryKeys.DAMAGE_TYPE)),
            (data, sourceAndAmount) -> condition(sourceAndAmount.getLeft(),
                data.get("tag")
            )
        );
    }

    public static ConditionTypeFactory<Pair<DamageSource, Float>> createFactory(Identifier id, TagKey<DamageType> damageTypeTag) {
        return new ConditionTypeFactory<>(id,
            new SerializableData(),
            (data, sourceAndAmount) -> condition(sourceAndAmount.getLeft(), damageTypeTag)
        );
    }

}
