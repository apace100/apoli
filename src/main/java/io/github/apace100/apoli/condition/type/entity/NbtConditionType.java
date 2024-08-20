package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;

public class NbtConditionType {

    public static boolean condition(Entity entity, NbtCompound nbt) {
        return NbtHelper.matches(nbt, entity.writeNbt(new NbtCompound()), true);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("nbt"),
            new SerializableData()
                .add("nbt", SerializableDataTypes.NBT_COMPOUND),
            (data, entity) -> condition(entity,
                data.get("nbt")
            )
        );
    }

}
