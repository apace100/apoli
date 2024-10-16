package io.github.apace100.apoli.condition.type.damage;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.DamageConditionType;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class InTagDamageConditionType extends DamageConditionType {

    public static final DataObjectFactory<InTagDamageConditionType>  DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("tag", SerializableDataType.tagKey(RegistryKeys.DAMAGE_TYPE)),
        data -> new InTagDamageConditionType(
            data.get("tag")
        ),
        (t, serializableData) -> serializableData.instance()
            .set("tag", t.tag)
    );

    private final TagKey<DamageType> tag;

    public InTagDamageConditionType(TagKey<DamageType> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(DamageSource source, float amount) {
        return source.isIn(tag);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return DamageConditionTypes.IN_TAG;
    }

}
