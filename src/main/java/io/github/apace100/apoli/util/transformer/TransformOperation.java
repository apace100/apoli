package io.github.apace100.apoli.util.transformer;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

public interface TransformOperation {

    SerializableDataType<TransformOperation> DATA_TYPE = SerializableDataType.registry(TransformOperation.class, ApoliRegistries.TRANSFORM_OPERATION, Apoli.MODID, true);

    SerializableData getSerializableData();

    @Environment(EnvType.CLIENT)
    void apply(SerializableData.Instance data, MatrixStack matrices, int x, int y, float tickDelta);

}
