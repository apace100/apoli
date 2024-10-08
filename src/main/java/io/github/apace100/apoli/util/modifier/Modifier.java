package io.github.apace100.apoli.util.modifier;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class Modifier implements Comparable<Modifier> {

    private static final String TYPE_KEY = "operation";
	private static final SerializableData DATA = new SerializableData().add(TYPE_KEY, IModifierOperation.DATA_TYPE);

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
     *          .set("resource", {@linkplain io.github.apace100.apoli.power.PowerReference#of(net.minecraft.util.Identifier) PowerReference.of(Identifier.of("example:resource"))})
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
            return Integer.compare(thisOp.getOrder(), thatOp.getOrder());
        }

        else {
            return thisOp.getPhase().compareTo(thatOp.getPhase());
        }

    }

    public static final SerializableDataType<Modifier> DATA_TYPE = SerializableDataType.recursive(dataType -> SerializableDataType.of(
		new Codec<>() {

			@Override
			public <T> DataResult<Pair<Modifier, T>> decode(DynamicOps<T> ops, T input) {
				boolean root = dataType.isRoot();
				return ops.getMap(input)
					.flatMap(mapInput -> DATA.setRoot(root).decode(ops, mapInput)
						.flatMap(modifierData -> {
							IModifierOperation operation = modifierData.get(TYPE_KEY);
							return operation.getSerializableData().setRoot(root).decode(ops, mapInput)
								.map(operationData -> new Modifier(operation, operationData))
								.map(modifier -> Pair.of(modifier, input));
						}));
			}

			@Override
			public <T> DataResult<T> encode(Modifier input, DynamicOps<T> ops, T prefix) {

				RecordBuilder<T> mapBuilder = ops.mapBuilder();
				IModifierOperation operation = input.getOperation();

				mapBuilder.add(TYPE_KEY, IModifierOperation.DATA_TYPE.write(ops, operation));
				operation.getSerializableData().setRoot(dataType.isRoot()).encode(input.getData(), ops, mapBuilder);

				return mapBuilder.build(prefix);

			}

		},
		new PacketCodec<>() {

			@Override
			public Modifier decode(RegistryByteBuf buf) {

				IModifierOperation operation = IModifierOperation.DATA_TYPE.receive(buf);
				SerializableData.Instance operationData = operation.getSerializableData().setRoot(dataType.isRoot()).receive(buf);

				return new Modifier(operation, operationData);

			}

			@Override
			public void encode(RegistryByteBuf buf, Modifier value) {

				IModifierOperation operation = value.getOperation();

				IModifierOperation.DATA_TYPE.send(buf, operation);
				operation.getSerializableData().setRoot(dataType.isRoot()).send(buf, value.getData());

			}

		}
	));

    public static final SerializableDataType<List<Modifier>> LIST_TYPE = DATA_TYPE.list();

}
