package io.github.apace100.apoli.power;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapLike;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.factory.PowerTypes;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.PowerPayloadType;
import io.github.apace100.apoli.util.StrictPowerDataType;
import io.github.apace100.calio.Calio;
import io.github.apace100.calio.codec.StrictCodec;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.Validatable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Power implements Validatable {

    public static final String TYPE_KEY = "type";
    public static final String LOADING_PRIORITY_KEY = "loading_priority";

    public static final SerializableData DATA = new SerializableData()
        .add("id", SerializableDataTypes.IDENTIFIER)
        .add(TYPE_KEY, ApoliDataTypes.POWER_TYPE_FACTORY)
        .addFunctionedDefault("name", ApoliDataTypes.DEFAULT_TRANSLATABLE_TEXT, data -> createTranslatable(data.getId("id"), "name"))
        .addFunctionedDefault("description", ApoliDataTypes.DEFAULT_TRANSLATABLE_TEXT, data -> createTranslatable(data.getId("id"), "description"))
        .add("hidden", SerializableDataTypes.BOOLEAN, false);

    /**
     *  <p>A data type for parsing {@linkplain Power powers}. It can decode and encode normal powers and powers that use the {@link PowerTypes#MULTIPLE multiple} power type (and its sub-powers.)</p>
     *  <p>However, evaluation of the powers' loading priority and resource condition must be handled <b>manually</b> (like in {@link PowerManager}.)</p>
     */
    public static final SerializableDataType<Power> DATA_TYPE = SerializableDataType.of(
        new StrictCodec<>() {

            @Override
            public <I> Pair<Power, I> strictDecode(DynamicOps<I> ops, I input) {

                Power power = StrictPowerDataType.INSTANCE.strictParse(ops, input);
                MapLike<I> mapInput = ops.getMap(input).getOrThrow();

                if (power.isMultiple()) {

                    Set<SubPower> subPowers = new LinkedHashSet<>();
                    mapInput.entries().forEach(keyAndValue -> {

                        String subPowerName = ops.getStringValue(keyAndValue.getFirst()).getOrThrow();
                        if (PowerManager.shouldIgnoreField(subPowerName)) {
                            return;
                        }

                        try {

                            Map<I, I> subMap = new LinkedHashMap<>();

                            subMap.put(ops.createString("id"), ops.createString(power.getId() + "_" + subPowerName));
                            ops.getMapEntries(keyAndValue.getSecond())
                                .getOrThrow()
                                .accept(subMap::put);

                            Power baseSubPower = StrictPowerDataType.INSTANCE.strictParse(ops, ops.createMap(subMap));
                            SubPower subPower = new SubPower(power.getId(), subPowerName, baseSubPower);

                            if (baseSubPower.isMultiple()) {
                                throw new IllegalArgumentException("Using the 'multiple' power type in sub-powers is not allowed!");
                            }

                            subPowers.add(subPower);

                        }

                        catch (Exception e) {
                            Apoli.LOGGER.error("There was a problem reading sub-power \"{}\" in power \"{}\": {}", subPowerName, power.getId(), e.getMessage());
                        }

                    });

                    MultiplePower multiplePower = new MultiplePower(subPowers, power);
                    return Pair.of(multiplePower, input);

                }

                else {
                    return Pair.of(power, input);
                }

            }

            @Override
            public <I> I strictEncode(Power input, DynamicOps<I> ops, I prefix) {

                Map<I, I> encodedPower = new LinkedHashMap<>();

                PowerTypeFactory<?>.Instance instance = input.getFactoryInstance();
                SerializableData instanceSerializableData = instance.getSerializableData();

                DATA.getFields().forEach((name, field) -> {

                    encodedPower.put(ops.createString(name), field.dataType().strictEncodeStart(ops, input.getData().get(name)));

                    if (name.equals(TYPE_KEY)) {

                        if (input instanceof MultiplePower multiplePower) {
                            multiplePower
                                .getSubPowersInternal()
                                .forEach(subPower -> encodedPower.put(ops.createString(subPower.getSubName()), StrictPowerDataType.INSTANCE.strictEncodeStart(ops, subPower)));
                        }

                        ops.getMapEntries(instanceSerializableData.codec().strictEncodeStart(ops, instance.getData()))
                            .getOrThrow()
                            .accept(encodedPower::put);

                    }

                });

                return ops.createMap(encodedPower);

            }

        },
        StrictPowerDataType.INSTANCE.packetCodec()
    );

    private final PowerTypeFactory<? extends PowerType>.Instance factoryInstance;
    private final SerializableData.Instance data;

    private final Identifier id;

    private final Text name;
    private final Text description;

    private final boolean hidden;

    public Power(PowerTypeFactory<? extends PowerType>.Instance factoryInstance, SerializableData.Instance data) {
        this.factoryInstance = factoryInstance;
        this.data = data;
        this.id = data.getId("id");
        this.name = data.get("name");
        this.description = data.get("description");
        this.hidden = data.getBoolean("hidden");
    }

    public static Power fromJson(Identifier id, JsonObject jsonObject) {
        jsonObject.addProperty("id", id.toString());
        return DATA_TYPE.strictParse(Calio.wrapRegistryOps(JsonOps.INSTANCE), jsonObject);
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

    public SerializableData.Instance getData() {
        return data;
    }

    public PowerType create(@Nullable LivingEntity entity) {
        return factoryInstance.apply(this, entity);
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

    public void send(RegistryByteBuf buf) {
        buf.writeEnumConstant(PowerPayloadType.POWER);
        this.sendInternal(buf);
    }

    protected final void sendInternal(RegistryByteBuf buf) {

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
                        new MultiplePower(basePower, buf.readCollection(HashSet::new, PacketByteBuf::readIdentifier));
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
        return DATA_TYPE.strictEncodeStart(JsonOps.INSTANCE, this).getAsJsonObject();
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
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
