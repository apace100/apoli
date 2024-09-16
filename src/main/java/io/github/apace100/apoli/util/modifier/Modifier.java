package io.github.apace100.apoli.util.modifier;

import com.mojang.serialization.*;
import io.github.apace100.calio.codec.CompoundMapCodec;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Modifier implements Comparable<Modifier> {

    public static final String TYPE_KEY = "operation";

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

    public static final SerializableDataType<Modifier> DATA_TYPE = new CompoundSerializableDataType<>(
        new SerializableData()
            .add(TYPE_KEY, IModifierOperation.DATA_TYPE),
        serializableData -> new CompoundMapCodec<>() {

			@Override
			public <T> Modifier fromData(DynamicOps<T> ops, SerializableData.Instance data) {
				IModifierOperation operation = data.get(TYPE_KEY);
				return new Modifier(operation, data);
			}

			@Override
			public <T> SerializableData.Instance toData(Modifier input, DynamicOps<T> ops, SerializableData serializableData) {

				SerializableData.Instance data = serializableData.instance();
				IModifierOperation operation = input.getOperation();

				data.set(TYPE_KEY, operation);
				operation.getSerializableData().getFieldNames().forEach(name -> data.set(name, input.getData().get(name)));

				return data;

			}

			@Override
			public <T> Stream<T> keys(DynamicOps<T> ops) {
				return serializableData.keys(ops);
			}

			@Override
			public <T> DataResult<Modifier> decode(DynamicOps<T> ops, MapLike<T> mapInput) {
				return serializableData.decode(ops, mapInput)
					.map(modifierData -> (IModifierOperation) modifierData.get(TYPE_KEY))
					.flatMap(operation -> operation.getSerializableData().setRoot(serializableData.isRoot()).decode(ops, mapInput)
						.map(operationData -> new Modifier(operation, operationData)));
			}

			@Override
			public <T> RecordBuilder<T> encode(Modifier input, DynamicOps<T> ops, RecordBuilder<T> prefix) {

				IModifierOperation operation = input.getOperation();

				prefix.add(TYPE_KEY, IModifierOperation.DATA_TYPE.codec().encodeStart(ops, operation));
				operation.getSerializableData().setRoot(serializableData.isRoot()).encode(input.getData(), ops, prefix);

				return prefix;

			}

		},
        (serializableData, compoundMapCodec) -> new PacketCodec<>() {

			@Override
			public Modifier decode(RegistryByteBuf buf) {

                IModifierOperation operation = IModifierOperation.DATA_TYPE.receive(buf);
                SerializableData.Instance operationData = operation.getSerializableData().receive(buf);

                return new Modifier(operation, operationData);

			}

			@Override
			public void encode(RegistryByteBuf buf, Modifier value) {

                IModifierOperation operation = value.getOperation();

                IModifierOperation.DATA_TYPE.send(buf, operation);
                operation.getSerializableData().send(buf, value.getData());

			}

		}
    );

    public static final SerializableDataType<List<Modifier>> LIST_TYPE = DATA_TYPE.list();

}
