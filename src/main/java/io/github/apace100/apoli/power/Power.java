package io.github.apace100.apoli.power;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.type.PowerTypes;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.PowerPayloadType;
import io.github.apace100.calio.Calio;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.Validatable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class Power implements Validatable {

    public static final String TYPE_KEY = "type";

    public static final SerializableData DATA = new SerializableData()
        .add("id", SerializableDataTypes.IDENTIFIER)
        .add(TYPE_KEY, ApoliDataTypes.POWER_TYPE_FACTORY)
        .addFunctionedDefault("name", ApoliDataTypes.DEFAULT_TRANSLATABLE_TEXT, data -> createTranslatable(data.getId("id"), "name"))
        .addFunctionedDefault("description", ApoliDataTypes.DEFAULT_TRANSLATABLE_TEXT, data -> createTranslatable(data.getId("id"), "description"))
        .add("hidden", SerializableDataTypes.BOOLEAN, false);

    private static final Codec<Power> STRICT_CODEC = new Codec<>() {

        @Override
        public <I> DataResult<Pair<Power, I>> decode(DynamicOps<I> ops, I input) {
            return ops.getMap(input)
                .flatMap(mapInput -> DATA.decode(ops, mapInput)
                    .flatMap(powerData -> {
                        PowerTypeFactory<?> factory = powerData.get(TYPE_KEY);
                        return factory.getSerializableData().decode(ops, mapInput)
                            .map(factory::fromData)
                            .map(instance -> new Power(instance, powerData))
                            .map(power -> Pair.of(power, input));
                    }));
        }

        @Override
        public <I> DataResult<I> encode(Power input, DynamicOps<I> ops, I prefix) {

            RecordBuilder<I> mapBuilder = ops.mapBuilder();
            PowerTypeFactory<?>.Instance instance = input.getFactoryInstance();

            DATA.getFields().forEach((name, field) -> {

                mapBuilder.add(name, field.dataType().codec().encodeStart(ops, input.data.get(name)));

                if (name.equals(TYPE_KEY)) {
                    instance.getSerializableData().encode(instance.getData(), ops, mapBuilder);
                }

            });

            return mapBuilder.build(prefix);

        }

    };

    public static final Codec<Power> CODEC = new Codec<>() {

        @Override
        public <T> DataResult<Pair<Power, T>> decode(DynamicOps<T> ops, T input) {
            return STRICT_CODEC.decode(ops, input);
        }

        @Override
        public <T> DataResult<T> encode(Power input, DynamicOps<T> ops, T prefix) {

            RecordBuilder<T> mapBuilder = ops.mapBuilder();
            PowerTypeFactory<?>.Instance instance = input.getFactoryInstance();

            DATA.getFields().forEach((name, field) -> {

                mapBuilder.add(name, field.dataType().codec().encodeStart(ops, input.data.get(name)));

                if (name.equals(TYPE_KEY)) {

                    if (input instanceof MultiplePower multiplePower) {
                        multiplePower
                            .getSubPowers()
                            .forEach(subPower -> mapBuilder.add(subPower.getSubName(), STRICT_CODEC.encodeStart(ops, subPower)));
                    }

                    instance.getSerializableData().encode(instance.getData(), ops, mapBuilder);

                }

            });

            return mapBuilder.build(prefix);

        }

    };

    protected final SerializableData.Instance data;

    private final PowerTypeFactory<? extends PowerType>.Instance factoryInstance;
    private final Identifier id;

    private final Text name;
    private final Text description;

    private final boolean hidden;

    protected Power(PowerTypeFactory<? extends PowerType>.Instance factoryInstance, SerializableData.Instance data) {
        this.factoryInstance = factoryInstance;
        this.data = data;
        this.id = data.getId("id");
        this.name = data.get("name");
        this.description = data.get("description");
        this.hidden = data.getBoolean("hidden");
    }

    public static Power fromJson(Identifier id, JsonObject jsonObject) {
        jsonObject.addProperty("id", id.toString());
        return CODEC.parse(Calio.wrapRegistryOps(JsonOps.INSTANCE), jsonObject).getOrThrow();
    }

    @Override
    public void validate() throws Exception {
        this.getFactoryInstance().validate();
    }

    public Identifier getId() {
        return id;
    }

    public PowerTypeFactory<? extends PowerType>.Instance getFactoryInstance() {
        return factoryInstance;
    }

    public PowerType create(@Nullable LivingEntity entity) {
        return this.getFactoryInstance().apply(this, entity);
    }

    public boolean isHidden() {
        return this.isSubPower()
            || this.hidden;
    }

    public final boolean isMultiple() {
        return this.getFactoryInstance().getFactory() == PowerTypes.MULTIPLE
            || this instanceof MultiplePower;
    }

    public final boolean isSubPower() {
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

    public PowerPayloadType payloadType() {
        return PowerPayloadType.POWER;
    }

    public void send(RegistryByteBuf buf) {

        buf.writeEnumConstant(this.payloadType());
        buf.writeIdentifier(this.getId());

        if (this.getFactoryInstance() != null) {

            buf.writeBoolean(true);
            this.getFactoryInstance().send(buf);

            SerializableDataTypes.TEXT.send(buf, this.getName());
            SerializableDataTypes.TEXT.send(buf, this.getDescription());

            buf.writeBoolean(this.isHidden());

        }

        else {
            buf.writeBoolean(false);
        }

    }

    public static Power receive(RegistryByteBuf buf) {

        PowerPayloadType payloadType = buf.readEnumConstant(PowerPayloadType.class);
        Identifier id = buf.readIdentifier();

        try {

            SerializableData.Instance data = DATA.instance().set("id", id);
            if (buf.readBoolean()) {

                PowerTypeFactory<?>.Instance powerType = ApoliRegistries.POWER_FACTORY
                    .getOrEmpty(buf.readIdentifier())
                    .orElseThrow()
                    .receive(buf);

                data.set(TYPE_KEY, powerType.getFactory());
                data.set("name", SerializableDataTypes.TEXT.receive(buf));
                data.set("description", SerializableDataTypes.TEXT.receive(buf));
                data.set("hidden", buf.readBoolean());

                Power basePower = new Power(powerType, data);
                return switch (payloadType) {
                    case MULTIPLE_POWER ->
                        new MultiplePower(basePower, buf.readCollection(LinkedHashSet::new, PacketByteBuf::readIdentifier));
                    case SUB_POWER ->
                        new SubPower(buf.readIdentifier(), buf.readString(), basePower);
                    default ->
                        basePower;
                };

            }

            else {
                throw new IllegalStateException("Missing power type!");
            }

        }

        catch (Exception e) {
            throw new IllegalStateException("Couldn't receive power \"" + id + "\": " + e.getMessage());
        }

    }

    public JsonObject toJson() {
        return CODEC.encodeStart(JsonOps.INSTANCE, this)
            .getOrThrow()
            .getAsJsonObject();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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

    @Override
    public String toString() {
        return this.toJson().toString();
    }

    public static Text createTranslatable(Identifier id, String type) {
        String translationKey = Util.createTranslationKey("power", id) + "." + type;
        return Text.translatable(translationKey);
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
