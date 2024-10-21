package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 *  TODO: Use {@link SerializableDataTypes#NBT_PATH} for the 'nbt' parameter    -eggohito
 */
public class NbtBlockConditionType extends BlockConditionType {

    public static final TypedDataObjectFactory<NbtBlockConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("nbt", SerializableDataTypes.NBT_COMPOUND),
        data -> new NbtBlockConditionType(
            data.get("nbt")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("nbt", conditionType.nbt)
    );

    private final NbtCompound nbt;

    public NbtBlockConditionType(NbtCompound nbt) {
        this.nbt = nbt;
    }

    @Override
    public boolean test(World world, BlockPos pos) {

        DynamicRegistryManager dynamicRegistries = world.getRegistryManager();
        BlockEntity blockEntity = world.getBlockEntity(pos);

        return blockEntity != null
            && NbtHelper.matches(nbt, blockEntity.createNbtWithIdentifyingData(dynamicRegistries), true);

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return BlockConditionTypes.NBT;
    }

}
