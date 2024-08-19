package io.github.apace100.apoli.power;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapLike;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.factory.PowerTypes;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.PowerPayloadType;
import io.github.apace100.calio.Calio;
import io.github.apace100.calio.codec.StrictCodec;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.Validatable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Power implements Validatable {

    public static final String TYPE_KEY = "type";

    public static final SerializableData DATA = new SerializableData()
        .add("id", SerializableDataTypes.IDENTIFIER)
        .add(TYPE_KEY, ApoliDataTypes.POWER_TYPE_FACTORY)
        .addFunctionedDefault("name", ApoliDataTypes.DEFAULT_TRANSLATABLE_TEXT, data -> createTranslatable(data.getId("id"), "name"))
        .addFunctionedDefault("description", ApoliDataTypes.DEFAULT_TRANSLATABLE_TEXT, data -> createTranslatable(data.getId("id"), "description"))
        .add("hidden", SerializableDataTypes.BOOLEAN, false);

    private static final StrictCodec<Power> STRICT_CODEC = new StrictCodec<>() {

        @Override
        public <I> Pair<Power, I> strictDecode(DynamicOps<I> ops, I input) {

            MapLike<I> mapInput = ops.getMap(input).getOrThrow();
            SerializableData.Instance powerData = Power.DATA.strictDecode(ops, mapInput);

            PowerTypeFactory<?> factory = powerData.get(Power.TYPE_KEY);
            SerializableData.Instance factoryData = factory.getSerializableData().strictDecode(ops, mapInput);

            return Pair.of(new Power(factory.fromData(factoryData), powerData), input);

        }

        @Override
        public <I> I strictEncode(Power input, DynamicOps<I> ops, I prefix) {

            Map<I, I> output = new LinkedHashMap<>();

            PowerTypeFactory<?>.Instance instance = input.getFactoryInstance();
            SerializableData instanceSerializableData = instance.getSerializableData();

            Power.DATA.getFields().forEach((name, field) -> {

                output.put(ops.createString(name), field.dataType().strictEncodeStart(ops, input.data.get(name)));

                if (name.equals(Power.TYPE_KEY)) {
                    ops.getMapEntries(instanceSerializableData.codec().strictEncodeStart(ops, instance.getData()))
                        .getOrThrow()
                        .accept(output::put);
                }

            });

            return ops.createMap(output);

        }

    };

    public static final StrictCodec<Power> CODEC = new StrictCodec<>() {

        @Override
        public <T> Pair<Power, T> strictDecode(DynamicOps<T> ops, T input) {
            return STRICT_CODEC.strictDecode(ops, input);
        }

        @Override
        public <T> T strictEncode(Power input, DynamicOps<T> ops, T prefix) {

            Map<T, T> output = new LinkedHashMap<>();

            PowerTypeFactory<?>.Instance instance = input.getFactoryInstance();
            SerializableData instanceSerializableData = instance.getSerializableData();

            Power.DATA.getFields().forEach((name, field) -> {

                output.put(ops.createString(name), field.dataType().strictEncodeStart(ops, input.data.get(name)));

                if (name.equals(Power.TYPE_KEY)) {

                    if (input instanceof MultiplePower multiplePower) {
                        multiplePower
                            .getSubPowers()
                            .forEach(subPower -> output.put(ops.createString(subPower.getSubName()), STRICT_CODEC.strictEncodeStart(ops, subPower)));
                    }

                    ops.getMapEntries(instanceSerializableData.codec().strictEncodeStart(ops, instance.getData()))
                        .getOrThrow()
                        .accept(output::put);

                }

            });

            return ops.createMap(output);

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
        return CODEC.strictParse(Calio.wrapRegistryOps(JsonOps.INSTANCE), jsonObject);
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
                    case MULTIPLE_POWER -> {

                        Set<SubPower> subPowers = new HashSet<>();
                        int count = buf.readVarInt();

                        for (int i = 0; i < count; i++) {

                            Power power = receive(buf);
                            if (power instanceof SubPower subPower) {
                                subPowers.add(subPower);
                            }

                            else {
                                throw new IllegalStateException("Received sub-power \"" + power.getId() + "\", which isn't an actual sub-power!");
                            }

                        }

                        yield new MultiplePower(basePower, subPowers);

                    }
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
        return CODEC.strictEncodeStart(JsonOps.INSTANCE, this).getAsJsonObject();
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

}
