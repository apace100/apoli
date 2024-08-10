package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;

import java.util.Objects;

public class ModifyTypeTagPowerType extends PowerType {

    private final TagKey<EntityType<?>> tag;

    public ModifyTypeTagPowerType(Power power, LivingEntity entity, TagKey<EntityType<?>> tag) {
        super(power, entity);
        this.tag = tag;
    }

    public static boolean doesApply(Entity entity, TagKey<EntityType<?>> entityTypeTag) {
        return PowerHolderComponent.hasPowerType(entity, ModifyTypeTagPowerType.class, p -> Objects.equals(p.tag, entityTypeTag));
    }

    public static boolean doesApply(Entity entity, RegistryEntryList<EntityType<?>> entryList) {
        return entryList.getTagKey()
            .map(tagKey -> doesApply(entity, tagKey))
            .orElse(false);
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_type_tag"),
            new SerializableData()
                .add("tag", SerializableDataTypes.ENTITY_TAG),
            data -> (powerType, livingEntity) -> new ModifyTypeTagPowerType(powerType, livingEntity,
                data.get("tag")
            )
        ).allowCondition();
    }

}
