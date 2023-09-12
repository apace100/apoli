package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;

public class SetEntityGroupPower extends Power implements Prioritized<SetEntityGroupPower> {

    private final EntityGroup group;
    private final int priority;

    public SetEntityGroupPower(PowerType<?> type, LivingEntity entity, EntityGroup group, int priority) {
        super(type, entity);
        this.group = group;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public EntityGroup getGroup() {
        return group;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("entity_group"),
            new SerializableData()
                .add("group", SerializableDataTypes.ENTITY_GROUP)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (type, player) -> new SetEntityGroupPower(
                type,
                player,
                data.get("group"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
