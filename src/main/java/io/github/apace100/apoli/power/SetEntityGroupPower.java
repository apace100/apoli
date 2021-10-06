package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;

public class SetEntityGroupPower extends Power {

    public final EntityGroup group;

    public SetEntityGroupPower(PowerType<?> type, LivingEntity entity, EntityGroup group) {
        super(type, entity);
        this.group = group;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("entity_group"),
            new SerializableData()
                .add("group", SerializableDataTypes.ENTITY_GROUP),
            data ->
                (type, player) -> new SetEntityGroupPower(type, player, (EntityGroup)data.get("group")))
            .allowCondition();
    }
}
