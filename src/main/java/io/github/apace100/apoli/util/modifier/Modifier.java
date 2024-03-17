package io.github.apace100.apoli.util.modifier;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.calio.data.DataException;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.Entity;
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

    public double apply(Entity entity, double value) {
        return operation.apply(entity, List.of(dataInstance), value, value);
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

    public static final SerializableDataType<Modifier> DATA_TYPE = new SerializableDataType<>(
        Modifier.class,
        (buffer, modifier) -> {
            IModifierOperation.DATA_TYPE.send(buffer, modifier.operation);
            modifier.operation.getData().write(buffer, modifier.dataInstance);
        },
        buffer -> {

            IModifierOperation operation = IModifierOperation.DATA_TYPE.receive(buffer);
            SerializableData.Instance data = operation.getData().read(buffer);

            return new Modifier(operation, data);

        },
        jsonElement -> {

            if (!(jsonElement instanceof JsonObject jsonObject)) {
                throw new JsonSyntaxException("Expected modifier to be a JSON object.");
            }

            if (!jsonObject.has("operation")) {
                throw new JsonSyntaxException("Modifier requires an \"operation\" field.");
            }

            try {

                IModifierOperation operation = IModifierOperation.DATA_TYPE.read(jsonObject.get("operation"));
                SerializableData.Instance data = operation.getData().read(jsonObject);

                return new Modifier(operation, data);

            } catch (Exception e) {
                throw new DataException(DataException.Phase.READING, "operation", e);
            }

        },
        modifier -> {

            JsonObject jsonObject = ModifierOperation.DATA.write(modifier.dataInstance);
            jsonObject.add("operation", IModifierOperation.DATA_TYPE.write(modifier.operation));

            return jsonObject;

        }
    );

    public static final SerializableDataType<List<Modifier>> LIST_TYPE = SerializableDataType.list(DATA_TYPE);

}
