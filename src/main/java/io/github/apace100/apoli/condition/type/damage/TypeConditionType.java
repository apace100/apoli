package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;

public class TypeConditionType {

    public static boolean condition(DamageSource damageSource, RegistryKey<DamageType> damageTypeKey) {
        return damageSource.isOf(damageTypeKey);
    }

    public static ConditionTypeFactory<Pair<DamageSource, Float>> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("type"),
            new SerializableData()
                .add("damage_type", SerializableDataTypes.DAMAGE_TYPE),
            (data, sourceAndAmount) -> condition(sourceAndAmount.getLeft(),
                data.get("damage_type"))
        );
    }

}
