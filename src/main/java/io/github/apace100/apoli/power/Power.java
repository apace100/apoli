package io.github.apace100.apoli.power;

import com.mojang.serialization.*;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.power.type.PowerTypes;
import io.github.apace100.apoli.util.TextUtil;
import io.github.apace100.calio.Calio;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.Validatable;
import io.netty.handler.codec.DecoderException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class Power implements Validatable {

    public static final CompoundSerializableDataType<Power> DATA_TYPE = new CompoundSerializableDataType<>(
        new SerializableData()
            .add("id", SerializableDataTypes.IDENTIFIER)
            .add("type", ApoliDataTypes.POWER_TYPE_FACTORY)
            .add("name", SerializableDataTypes.TEXT, null)
            .add("description", SerializableDataTypes.TEXT, null)
            .add("hidden", SerializableDataTypes.BOOLEAN, false),
        serializableData -> {
            boolean root = serializableData.isRoot();
            return MapCodec.recursive("Power", self -> new MapCodec<>() {

				@Override
				public <T> Stream<T> keys(DynamicOps<T> ops) {
					return serializableData.keys(ops);
				}

				@Override
				public <T> DataResult<Power> decode(DynamicOps<T> ops, MapLike<T> input) {

                    DataResult<SerializableData.Instance> powerDataResult = serializableData.decode(ops, input);
                    DataResult<PowerTypeFactory<?>.Instance> instanceResult = powerDataResult
                        .map(powerData -> (PowerTypeFactory<?>) powerData.get("type"))
                        .flatMap(factory -> factory.getSerializableData().setRoot(root).decode(ops, input)
                            .map(factory::fromData));

                    return powerDataResult
                        .flatMap(powerData -> instanceResult
                            .map(instance -> new Power(instance, powerData)));

				}

				@Override
				public <T> RecordBuilder<T> encode(Power input, DynamicOps<T> ops, RecordBuilder<T> prefix) {

                    PowerTypeFactory<?>.Instance instance = input.getFactoryInstance();
					prefix.add("type", ApoliDataTypes.POWER_TYPE_FACTORY.write(ops, instance.getFactory()));

					if (input instanceof MultiplePower multiplePower) {
						multiplePower
							.getSubPowers()
							.forEach(subPower -> prefix.add(subPower.getSubName(), self.encodeStart(ops, subPower)));
					}

                    instance.getSerializableData().encode(instance.getData(), ops, prefix);

					prefix.add("name", SerializableDataTypes.TEXT.write(ops, input.getName()));
					prefix.add("description", SerializableDataTypes.TEXT.write(ops, input.getDescription()));
					prefix.add("hidden", ops.createBoolean(input.isHidden()));

					return prefix;

				}

			});
        },
        serializableData -> PacketCodec.ofStatic(
			(buf, value) -> {

                PowerTypeFactory<?>.Instance instance = value.getFactoryInstance();
                if (instance == null) {
                    return;
                }

                buf.writeIdentifier(value.getId());
                serializableData.send(buf, serializableData.instance()
                    .set("id", value.getId())
                    .set("type", instance.getFactory())
                    .set("name", value.getName())
                    .set("description", value.getDescription())
                    .set("hidden", value.isHidden()));

                instance.getSerializableData().send(buf, instance.getData());
                switch (value) {
                    case MultiplePower multiplePower -> {
                        buf.writeByte(0);
                        MultiplePower.PACKET_CODEC.apply(multiplePower).encode(buf, multiplePower);
                    }
                    case SubPower subPower -> {
                        buf.writeByte(1);
                        SubPower.PACKET_CODEC.apply(subPower).encode(buf, subPower);
                    }
                    default ->
                        buf.writeByte(Byte.MAX_VALUE);
                }

            },
            buf -> {

                Identifier powerId = buf.readIdentifier();
                SerializableData.Instance powerData = serializableData.receive(buf);

                try {

                    PowerTypeFactory<?> factory = powerData.get("type");
                    Power basePower = new Power(factory.receive(buf), powerData);

                    return switch (buf.readByte()) {
                        case 0 ->
                            MultiplePower.PACKET_CODEC.apply(basePower).decode(buf);
                        case 1 ->
                            SubPower.PACKET_CODEC.apply(basePower).decode(buf);
                        default ->
                            basePower;
                    };

                }

                catch (Exception e) {
                    throw new DecoderException("Couldn't receive power \"" + powerId + "\": " + e);
                }

            }
        )
    );

    private final Identifier id;
    private final PowerTypeFactory<? extends PowerType>.Instance factoryInstance;

    private final Text name;
    private final Text description;

    private final boolean hidden;

    protected Power(Identifier id, PowerTypeFactory<? extends PowerType>.Instance factoryInstance, @Nullable Text name, @Nullable Text description, boolean hidden) {

        this.id = id;
        String baseTranslationKey = Util.createTranslationKey("power", id);

        this.factoryInstance = factoryInstance;
        this.name = TextUtil.forceTranslatable(baseTranslationKey + ".name", Optional.ofNullable(name));
        this.description = TextUtil.forceTranslatable(baseTranslationKey + ".description", Optional.ofNullable(description));
        this.hidden = hidden;

    }

    protected Power(PowerTypeFactory<?>.Instance instance, SerializableData.Instance data) {
        this(data.get("id"), instance, data.get("name"), data.get("description"), data.get("hidden"));
    }

    protected Power(Power basePower) {
        this(basePower.getId(), basePower.getFactoryInstance(), basePower.getName(), basePower.getDescription(), basePower.isHidden());
    }

    @Override
    public void validate() throws Exception {
        this.getFactoryInstance().validate();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        else if (obj instanceof Power that) {
            return Objects.equals(this.getId(), that.getId());
        }

        else {
            return false;
        }

    }

    public Identifier getId() {
        return id;
    }

    public PowerTypeFactory<?>.Instance getFactoryInstance() {
        return factoryInstance;
    }

    public PowerType create(@Nullable LivingEntity entity) {
        return this.getFactoryInstance().apply(this, entity);
    }

    public boolean isHidden() {
        return this.isSubPower()
            || this.hidden;
    }

    public boolean isMultiple() {
        return this.getFactoryInstance().getFactory() == PowerTypes.MULTIPLE
            || this instanceof MultiplePower;
    }

    public boolean isSubPower() {
        return this instanceof SubPower;
    }

    public boolean isActive(Entity entity) {
        PowerType powerType = this.getType(entity);
        return powerType != null
            && powerType.isActive();
    }

    @Nullable
    public PowerType getType(Entity entity) {

        if (entity != null && PowerHolderComponent.KEY.isProvidedBy(entity)) {
            return PowerHolderComponent.KEY.get(entity).getPowerType(this);
        }

        else {
            return null;
        }

    }

    public MutableText getName() {
        return name.copy();
    }

    public MutableText getDescription() {
        return description.copy();
    }

    public record Entry(PowerTypeFactory<?> typeFactory, PowerReference power, @Nullable NbtElement nbtData, List<Identifier> sources) {

        private static final SerializableDataType<List<Identifier>> MUTABLE_IDENTIFIERS = SerializableDataTypes.IDENTIFIER.list(1, Integer.MAX_VALUE).xmap(LinkedList::new, Function.identity());

        public static final SerializableDataType<Entry> CODEC = SerializableDataType.compound(
            new SerializableData()
                .add("Factory", ApoliDataTypes.POWER_TYPE_FACTORY, null)
                .addFunctionedDefault("type", ApoliDataTypes.POWER_TYPE_FACTORY, data -> data.get("Factory"))
                .add("Type", ApoliDataTypes.POWER_REFERENCE, null)
                .addFunctionedDefault("id", ApoliDataTypes.POWER_REFERENCE, data -> data.get("Type"))
                .add("Data", SerializableDataTypes.NBT_ELEMENT, null)
                .addFunctionedDefault("data", SerializableDataTypes.NBT_ELEMENT, data -> data.get("Data"))
                .add("Sources", MUTABLE_IDENTIFIERS, null)
                .addFunctionedDefault("sources", MUTABLE_IDENTIFIERS, data -> data.get("Sources"))
                .validate(data -> {

                    if (!data.isPresent("type")) {
                        return Calio.createMissingRequiredFieldError("type");
                    }

                    else if (!data.isPresent("id")) {
                        return Calio.createMissingRequiredFieldError("id");
                    }

                    else if (!data.isPresent("sources")) {
                        return Calio.createMissingRequiredFieldError("sources");
                    }

                    else {
                        return DataResult.success(data);
                    }

                }),
            data -> new Entry(
                data.get("type"),
                data.get("id"),
                data.get("data"),
                data.get("sources")
            ),
            (entry, serializableData) -> serializableData.instance()
                .set("type", entry.typeFactory())
                .set("id", entry.power())
                .set("data", entry.nbtData())
                .set("sources", entry.sources())
        );

    }

}
