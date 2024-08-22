package io.github.apace100.apoli.util;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapLike;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.codec.StrictCodec;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.network.codec.PacketCodec;

import java.util.LinkedHashMap;
import java.util.Map;

public class StrictPowerDataType extends SerializableDataType<Power> {

    public static final StrictPowerDataType INSTANCE = new StrictPowerDataType();

    private StrictPowerDataType() {
        super(createBaseCodec(), PacketCodec.of(Power::send, Power::receive), "Power");
    }

    private static StrictCodec<Power> createBaseCodec() {
        return new StrictCodec<>() {

            @Override
            public <I> Pair<Power, I> strictDecode(DynamicOps<I> ops, I input) {

                MapLike<I> mapInput = ops.getMap(input).getOrThrow();
                SerializableData.Instance powerData = Power.DATA.strictDecode(ops, mapInput);

                PowerTypeFactory<?> factory = powerData.get(Power.TYPE_KEY);
                SerializableData.Instance factoryData = factory.getSerializableData().strictDecode(ops, mapInput);

                JsonObject resourceConditionJson = new JsonObject();
                I resourceConditionInput = mapInput.get(ResourceConditions.CONDITIONS_KEY);

                if (resourceConditionInput != null) {
                    resourceConditionJson.add(ResourceConditions.CONDITIONS_KEY, ops.convertTo(JsonOps.INSTANCE, resourceConditionInput));
                }

                //  Store the power's loading priority, and resource condition for further processing at a later date
                powerData.set(Power.LOADING_PRIORITY_KEY, ops.getNumberValue(mapInput.get(Power.LOADING_PRIORITY_KEY), 0).intValue());
                powerData.set(ResourceConditions.CONDITIONS_KEY, resourceConditionJson);

                return Pair.of(new Power(factory.fromData(factoryData), powerData), input);

            }

            @Override
            public <I> I strictEncode(Power input, DynamicOps<I> ops, I prefix) {

                Map<I, I> encodedPower = new LinkedHashMap<>();

                PowerTypeFactory<?>.Instance instance = input.getFactoryInstance();
                SerializableData instanceSerializableData = instance.getSerializableData();

                Power.DATA.getFields().forEach((name, field) -> {

                    encodedPower.put(ops.createString(name), field.dataType().strictEncodeStart(ops, input.getData().get(name)));

                    if (name.equals(Power.TYPE_KEY)) {
                        ops.getMapEntries(instanceSerializableData.codec().strictEncodeStart(ops, instance.getData()))
                            .getOrThrow()
                            .accept(encodedPower::put);
                    }

                });

                return ops.createMap(encodedPower);

            }

        };
    }

}
