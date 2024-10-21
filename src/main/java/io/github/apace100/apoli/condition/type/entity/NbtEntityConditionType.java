package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;

/**
 *  TODO: Use {@link SerializableDataTypes#NBT_PATH} for the 'nbt' parameter    -eggohito
 */
public class NbtEntityConditionType extends EntityConditionType {

    public static final TypedDataObjectFactory<NbtEntityConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("nbt", SerializableDataTypes.NBT_COMPOUND),
        data -> new NbtEntityConditionType(
            data.get("nbt")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("nbt", conditionType.nbt)
    );

    private final NbtCompound nbt;

    public NbtEntityConditionType(NbtCompound nbt) {
        this.nbt = nbt;
    }

    @Override
    public boolean test(Entity entity) {
        return NbtHelper.matches(nbt, entity.writeNbt(new NbtCompound()), true);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.NBT;
    }

}
