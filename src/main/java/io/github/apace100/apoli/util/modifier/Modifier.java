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
import java.util.function.Consumer;

public class Modifier implements Comparable<Modifier> {

    public static final String TYPE_KEY = "operation";
    public static final SerializableData DATA = new SerializableData().add(TYPE_KEY, IModifierOperation.DATA_TYPE);

    private final IModifierOperation operation;
    private final SerializableData.Instance data;

    protected Modifier(IModifierOperation operation, SerializableData.Instance data) {
        this.operation = operation;
        this.data = data;
    }

    /**
     *  <p>Constructs a {@link Modifier} with the given {@link IModifierOperation} and its {@link SerializableData.Instance} that will be processed;
     *  useful for defining the values of individual fields of the {@link IModifierOperation}.<p>
     *
     *  <p>e.g:</p>
     *  <pre>
     *      Modifier modifier = Modifier.of({@linkplain ModifierOperation#SET_TOTAL}, data -> data
     *          .set("resource", {@linkplain io.github.apace100.apoli.power.PowerReference#of(String, String) PowerReference.of("example", "resource")})
     *          .set("modifier", {@linkplain #of(ModifierOperation, double) Modifier.of(}{@linkplain ModifierOperation#ADD_BASE_EARLY}{@linkplain #of(ModifierOperation, double), 1.0)});
     *  </pre>
     *
     *  <p>or if you want to use a custom modifier operation:</p>
     *  <pre>
     *      Modifier modifier = Modifier.of(ExampleOperation.BASE_DIVISION, data -> data
     *          .set("divisor", 2));    //  Assuming this field uses {@linkplain io.github.apace100.calio.data.SerializableDataTypes#POSITIVE_INT SerializableDataTypes#POSITIVE_INT}
     *  </pre>
     */
    public static Modifier of(IModifierOperation operation, Consumer<SerializableData.Instance> processor) {

        SerializableData.Instance data = operation.getSerializableData().instance();
        processor.accept(data);

        return new Modifier(operation, data);

    }

    /**
     *  Constructs a {@link Modifier} that specifically use a {@link ModifierOperation}. For defining values for fields of custom implementations of {@link IModifierOperation},
     *  <b>use {@link #of(IModifierOperation, Consumer) Modifier#of(IModifierOperation, Consumer&lt;SerializableData.Instance&gt;)} instead.</b>
     */
    public static Modifier of(ModifierOperation operation, double amount) {
        return of(operation, data -> data.set("amount", amount));
    }

    public IModifierOperation getOperation() {
        return operation;
    }

    public SerializableData.Instance getData() {
        return data;
    }

    public double apply(Entity entity, double value) {
        return operation.apply(entity, List.of(data), value, value);
    }

    @Override
    public int compareTo(@NotNull Modifier that) {

        IModifierOperation thisOp = this.getOperation();
        IModifierOperation thatOp = that.getOperation();

        if (thisOp.equals(thatOp)) {
            return 0;
        }

        else if (thisOp.getPhase() == thatOp.getPhase()) {
            return thatOp.getOrder() - getOperation().getOrder();
        }

        else {
            return thatOp.getPhase() == IModifierOperation.Phase.BASE ? 1 : -1;
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
                operation.getSerializableData().encode(input.getData(), ops, ops.mapBuilder()).build(prefix)
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
