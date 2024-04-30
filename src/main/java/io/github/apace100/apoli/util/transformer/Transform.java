package io.github.apace100.apoli.util.transformer;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.calio.data.DataException;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

import java.util.List;

public record Transform(TransformOperation operation, SerializableData.Instance data) {

    public static final SerializableDataType<Transform> DATA_TYPE = new SerializableDataType<>(
        Transform.class,
        (buf, transform) -> {

            TransformOperation operation = transform.operation();

            TransformOperation.DATA_TYPE.send(buf, operation);
            operation.getSerializableData().write(buf, transform.data());

        },
        buf -> {

            TransformOperation operation = TransformOperation.DATA_TYPE.receive(buf);
            SerializableData.Instance data = operation.getSerializableData().read(buf);

            return new Transform(operation, data);

        },
        jsonElement -> {

            if (!(jsonElement instanceof JsonObject jsonObject)) {
                throw new JsonSyntaxException("Expected transform to be a JSON object.");
            }

            if (!jsonObject.has("operation")) {
                throw new JsonSyntaxException("Transform requires an \"operation\" field.");
            }

            try {

                TransformOperation operation = TransformOperation.DATA_TYPE.read(jsonObject.get("operation"));
                SerializableData.Instance data = operation.getSerializableData().read(jsonObject);

                return new Transform(operation, data);

            }

            catch (Exception e) {
                throw new DataException(DataException.Phase.READING, "operation", e);
            }

        },
        transform -> {

            TransformOperation operation = transform.operation();

            JsonObject jsonObject = operation.getSerializableData().write(transform.data());
            jsonObject.add("operation", TransformOperation.DATA_TYPE.write(operation));

            return jsonObject;

        }
    );

    public static final SerializableDataType<List<Transform>> LIST_TYPE = SerializableDataType.list(DATA_TYPE);

    @Environment(EnvType.CLIENT)
    public void apply(MatrixStack matrices, int x, int y, float tickDelta) {
        this.operation().apply(this.data(), matrices, x, y, tickDelta);
    }

}
