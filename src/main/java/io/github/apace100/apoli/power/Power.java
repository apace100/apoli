package io.github.apace100.apoli.power;

import com.mojang.serialization.*;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.power.type.PowerTypes;
import io.github.apace100.apoli.power.type.meta.MultiplePowerType;
import io.github.apace100.apoli.util.TextUtil;
import io.github.apace100.calio.Calio;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.Validatable;
import io.netty.handler.codec.DecoderException;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class Power implements Validatable {

    public static final SerializableData SERIALIZABLE_DATA = new SerializableData()
        .add("id", SerializableDataTypes.IDENTIFIER)
        .add("type", PowerTypes.DATA_TYPE)
        .add("name", SerializableDataTypes.TEXT.optional(), Optional.empty())
        .add("description", SerializableDataTypes.TEXT.optional(), Optional.empty())
        .add("hidden", SerializableDataTypes.BOOLEAN, false);

    @SuppressWarnings("unchecked")
    public static final SerializableDataType<Power> DATA_TYPE = SerializableDataType.lazy(() -> new CompoundSerializableDataType<>(
        SERIALIZABLE_DATA,
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
                    DataResult<PowerType> powerTypeResult = powerDataResult
                        .map(powerData -> (PowerConfiguration<PowerType>) powerData.get("type"))
                        .flatMap(config -> config.mapCodec(root).decode(ops, input));

                    return powerDataResult
                        .flatMap(powerData -> powerTypeResult
                            .map(powerType -> new Power(powerType, powerData)));

				}

				@Override
				public <T> RecordBuilder<T> encode(Power input, DynamicOps<T> ops, RecordBuilder<T> prefix) {

                    PowerType powerType = input.getPowerType();
                    PowerConfiguration<PowerType> config = (PowerConfiguration<PowerType>) powerType.getConfig();

                    prefix.add("type", PowerTypes.DATA_TYPE.write(ops, config));

                    if (input instanceof MultiplePower multiplePower) {
                        multiplePower
                            .getSubPowers()
                            .forEach(subPower -> prefix.add(subPower.getSubName(), self.encodeStart(ops, subPower)));
                    }

                    config.mapCodec(root).encode(powerType, ops, prefix);

                    prefix.add("name", SerializableDataTypes.TEXT.write(ops, input.getName()));
                    prefix.add("description", SerializableDataTypes.TEXT.write(ops, input.getDescription()));
                    prefix.add("hidden", ops.createBoolean(input.isHidden()));

                    return prefix;

				}

			});
        },
        serializableData -> new PacketCodec<>() {

			@Override
			public Power decode(RegistryByteBuf buf) {

                Identifier powerId = buf.readIdentifier();
                SerializableData.Instance powerData = serializableData.receive(buf);

                try {

                    PowerConfiguration<?> config = powerData.get("type");
                    PowerType powerType = config.dataType().receive(buf);

                    Power basePower = new Power(powerType, powerData);
                    byte type = buf.readByte();

                    return switch (type) {
                        case 0 ->
                            MultiplePower.PACKET_CODEC.apply(basePower).decode(buf);
                        case 1 ->
                            SubPower.PACKET_CODEC.apply(basePower).decode(buf);
                        case Byte.MAX_VALUE ->
                            basePower;
                        default ->
                            throw new IllegalStateException("Unknown type!");
                    };

                }

                catch (Exception e) {
                    throw new DecoderException("Couldn't receive power \"" + powerId + "\": " + e);
                }

			}

			@Override
			public void encode(RegistryByteBuf buf, Power value) {

                try {

                    PowerType powerType = value.getPowerType();
                    PowerConfiguration<PowerType> config = (PowerConfiguration<PowerType>) powerType.getConfig();

                    SerializableData.Instance powerData = serializableData.instance()
                        .set("id", value.getId())
                        .set("type", config)
                        .set("name", Optional.of(value.getName()))
                        .set("description", Optional.of(value.getDescription()))
                        .set("hidden", value.isHidden());

                    buf.writeIdentifier(value.getId());

                    serializableData.send(buf, powerData);
                    config.dataType().send(buf, powerType);

                    switch (value) {
                        case MultiplePower multiplePower -> {
                            buf.writeByte(0);
                            MultiplePower.PACKET_CODEC.apply(value).encode(buf, multiplePower);
                        }
                        case SubPower subPower -> {
                            buf.writeByte(1);
                            SubPower.PACKET_CODEC.apply(value).encode(buf, subPower);
                        }
                        default ->
                            buf.writeByte(Byte.MAX_VALUE);
                    }

                }

                catch (Exception e) {
                    Apoli.LOGGER.error("Error trying to send power \"{}\" with power type \"{}\": ", value.getId(), value.getPowerType().getConfig().id());
                    throw e;
                }

			}

		}
    ));


    private final Identifier id;
    private final PowerType powerType;

    private final Text name;
    private final Text description;

    private final boolean hidden;

    protected Power(Identifier id, PowerType powerType, Optional<Text> name, Optional<Text> description, boolean hidden) {

        this.id = id;
        this.powerType = powerType;

        String baseTranslationKey = Util.createTranslationKey("power", id);

        this.name = TextUtil.forceTranslatable(baseTranslationKey + ".name", name);
        this.description = TextUtil.forceTranslatable(baseTranslationKey + ".description", description);

        this.hidden = hidden;

    }

    protected Power(PowerType powerType, SerializableData.Instance data) {
        this(data.get("id"), powerType, data.get("name"), data.get("description"), data.get("hidden"));
    }

    protected Power(Power basePower) {
        this(basePower.getId(), basePower.getPowerType(), Optional.of(basePower.getName()), Optional.of(basePower.getDescription()), basePower.isHidden());
    }

    @Override
    public void validate() throws Exception {
        getPowerType().validate();
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

    @NotNull
    public PowerType getPowerType() {
        return powerType;
    }

    @Nullable
    public PowerType getPowerTypeFrom(Entity entity) {
        return PowerHolderComponent.getOptional(entity)
            .map(powerComponent -> powerComponent.getPowerType(this))
            .orElse(null);
    }

    public PowerReference asReference() {
        return PowerReference.of(this.getId());
    }

    public boolean isHidden() {
        return this.isSubPower()
            || this.hidden;
    }

    public boolean isMultiple() {
        return this.getPowerType() instanceof MultiplePowerType
            || this instanceof MultiplePower;
    }

    public boolean isSubPower() {
        return this instanceof SubPower;
    }

    public boolean isActive(Entity entity) {
        return PowerHolderComponent.getOptional(entity)
            .map(powerComponent -> powerComponent.getPowerType(this))
            .filter(PowerType::isInitialized)
            .map(PowerType::isActive)
            .orElse(false);
    }

    public MutableText getName() {
        return name.copy();
    }

    public MutableText getDescription() {
        return description.copy();
    }

    public record Entry(PowerConfiguration<?> typeConfig, PowerReference powerReference, NbtElement nbtData, List<Identifier> sources) {

        private static final SerializableDataType<List<Identifier>> MUTABLE_IDENTIFIERS = SerializableDataTypes.IDENTIFIER.list(1, Integer.MAX_VALUE).xmap(LinkedList::new, Function.identity());

        public static final SerializableDataType<Entry> CODEC = SerializableDataType.compound(
            new SerializableData()
                .add("Factory", PowerTypes.DATA_TYPE, null)
                .addFunctionedDefault("type", PowerTypes.DATA_TYPE, data -> data.get("Factory"))
                .add("Type", ApoliDataTypes.POWER_REFERENCE, null)
                .addFunctionedDefault("id", ApoliDataTypes.POWER_REFERENCE, data -> data.get("Type"))
                .add("Data", SerializableDataTypes.NBT_ELEMENT, new NbtCompound())
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
                .set("type", entry.typeConfig())
                .set("id", entry.powerReference())
                .set("data", entry.nbtData())
                .set("sources", entry.sources())
        );

    }

}
