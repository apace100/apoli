package io.github.apace100.apoli.util.modifier;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import io.github.apace100.calio.codec.StrictCodec;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.Entity;
import net.minecraft.network.codec.PacketCodec;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Modifier implements Comparable<Modifier> {

    public static final String TYPE_KEY = "operation";
    public static final SerializableData DATA = new SerializableData().add(TYPE_KEY, IModifierOperation.DATA_TYPE);

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

    public static final SerializableDataType<Modifier> DATA_TYPE = SerializableDataType.of(
        new StrictCodec<>() {

            @Override
            public <T> Pair<Modifier, T> strictDecode(DynamicOps<T> ops, T input) {

                MapLike<T> mapInput = ops.getMap(input).getOrThrow();

                SerializableData.Instance modifierData = DATA.strictDecode(ops, mapInput);
                IModifierOperation operation = modifierData.get(TYPE_KEY);

                SerializableData.Instance operationData = operation.getSerializableData().strictDecode(ops, mapInput);
                return Pair.of(new Modifier(operation, operationData), input);

            }

            @Override
            public <T> T strictEncode(Modifier input, DynamicOps<T> ops, T prefix) {

                Map<T, T> output = new LinkedHashMap<>();
                IModifierOperation operation = input.getOperation();

                output.put(ops.createString(TYPE_KEY), IModifierOperation.DATA_TYPE.strictEncodeStart(ops, operation));
                operation.getSerializableData().encode(input.getData(), ops, ops.mapBuilder()).build(ops.empty())
                    .flatMap(ops::getMapEntries)
                    .getOrThrow()
                    .accept(output::put);

                return ops.createMap(output);

            }

        },
        PacketCodec.ofStatic(
            (buf, modifier) -> {
                IModifierOperation.DATA_TYPE.send(buf, modifier.getOperation());
                modifier.getOperation().getSerializableData().send(buf, modifier.getData());
            },
            buf -> {

                IModifierOperation operation = IModifierOperation.DATA_TYPE.receive(buf);
                SerializableData.Instance data = operation.getSerializableData().receive(buf);

                return new Modifier(operation, data);

            }
        )
    );

    public static final SerializableDataType<List<Modifier>> LIST_TYPE = DATA_TYPE.listOf();

}
