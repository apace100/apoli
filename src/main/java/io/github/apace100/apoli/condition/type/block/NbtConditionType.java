package io.github.apace100.apoli.condition.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.DynamicRegistryManager;

public class NbtConditionType {

    public static boolean condition(CachedBlockPosition cachedBlock, NbtCompound nbt) {

        DynamicRegistryManager registryManager = cachedBlock.getWorld().getRegistryManager();
        BlockEntity blockEntity = cachedBlock.getBlockEntity();

        return blockEntity != null
            && NbtHelper.matches(nbt, blockEntity.createNbtWithIdentifyingData(registryManager), true);

    }

    public static ConditionTypeFactory<CachedBlockPosition> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("nbt"),
            new SerializableData()
                .add("nbt", SerializableDataTypes.NBT),
            (data, cachedBlock) -> condition(cachedBlock,
                data.get("nbt")
            )
        );
    }

}
