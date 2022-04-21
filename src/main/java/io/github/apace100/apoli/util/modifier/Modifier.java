package io.github.apace100.apoli.util.modifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.apace100.calio.data.DataException;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Modifier implements Comparable<Modifier> {

    private final IModifierOperation operation;
    private final SerializableData.Instance dataInstance;

    public Modifier(IModifierOperation operation, SerializableData.Instance dataInstance) {
        this.operation = operation;
        this.dataInstance = dataInstance;
    }

    public IModifierOperation getOperation() {
        return operation;
    }

    public SerializableData.Instance getData() {
        return dataInstance;
    }

    @Override
    public int compareTo(@NotNull Modifier o) {
        if(o.operation == operation) {
            return 0;
        } else if(o.operation.getPhase() == operation.getPhase()) {
            return o.operation.getOrder() - operation.getOrder();
        } else {
            return o.operation.getPhase() == IModifierOperation.Phase.BASE ? 1 : -1;
        }
    }

    public static final SerializableDataType<Modifier> DATA_TYPE = new SerializableDataType<>(Modifier.class,
        (packetByteBuf, modifier) -> {
            IModifierOperation.DATA_TYPE.send(packetByteBuf, modifier.operation);
            modifier.operation.getData().write(packetByteBuf, modifier.dataInstance);
        },
        (packetByteBuf -> {
            IModifierOperation operation = IModifierOperation.DATA_TYPE.receive(packetByteBuf);
            SerializableData.Instance instance = operation.getData().read(packetByteBuf);
            return new Modifier(operation, instance);
        }),
        (jsonElement -> {
            if(!jsonElement.isJsonObject()) {
                throw new JsonParseException("Modifiers need to be a JSON object.");
            }
            JsonObject jo = jsonElement.getAsJsonObject();
            if(!jo.has("operation")) {
                throw new JsonParseException("Modifiers need to contain an \"operation\" field.");
            }
            IModifierOperation op;
            try {
                op = IModifierOperation.DATA_TYPE.read(jo.get("operation"));
            } catch(Exception e) {
                throw new DataException(DataException.Phase.READING, "operation", e);
            }
            SerializableData.Instance dataInstance = op.getData().read(jo);
            return new Modifier(op, dataInstance);
        }));

    public static final SerializableDataType<List<Modifier>> LIST_TYPE = SerializableDataType.list(DATA_TYPE);
}
