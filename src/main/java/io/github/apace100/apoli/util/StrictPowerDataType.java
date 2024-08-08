package io.github.apace100.apoli.util;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapLike;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.codec.StrictCodec;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.network.codec.PacketCodec;

import java.util.LinkedHashMap;
import java.util.Map;

public class StrictPowerDataType extends SerializableDataType<PowerType> {

    public static final StrictPowerDataType INSTANCE = new StrictPowerDataType();

    private StrictPowerDataType() {
        super(createBaseCodec(), PacketCodec.of(PowerType::send, PowerType::receive), "Power");
    }

    private static StrictCodec<PowerType> createBaseCodec() {
        return new StrictCodec<>() {

            @Override
            public <I> Pair<PowerType, I> strictDecode(DynamicOps<I> ops, I input) {

                MapLike<I> mapInput = ops.getMap(input).getOrThrow();
                SerializableData.Instance powerData = PowerType.DATA.strictDecode(ops, mapInput);

                PowerFactory<?> factory = powerData.get(PowerType.TYPE_KEY);
                SerializableData.Instance factoryData = factory.getSerializableData().strictDecode(ops, mapInput);

                JsonObject resourceConditionJson = new JsonObject();
                I resourceConditionInput = mapInput.get(ResourceConditions.CONDITIONS_KEY);

                if (resourceConditionInput != null) {
                    resourceConditionJson.add(ResourceConditions.CONDITIONS_KEY, ops.convertTo(JsonOps.INSTANCE, resourceConditionInput));
                }

                //  Store the power's loading priority, and resource condition for further processing at a later date
                powerData.set(PowerType.LOADING_PRIORITY_KEY, ops.getNumberValue(mapInput.get(PowerType.LOADING_PRIORITY_KEY), 0).intValue());
                powerData.set(ResourceConditions.CONDITIONS_KEY, resourceConditionJson);

                return Pair.of(new PowerType(factory.fromData(factoryData), powerData), input);

            }

            @Override
            public <I> I strictEncode(PowerType input, DynamicOps<I> ops, I prefix) {

                Map<I, I> encodedPower = new LinkedHashMap<>();

                PowerFactory<?>.Instance instance = input.getFactoryInstance();
                SerializableData instanceSerializableData = instance.getSerializableData();

                PowerType.DATA.getFields().forEach((name, field) -> {

                    encodedPower.put(ops.createString(name), field.dataType().strictEncodeStart(ops, input.getData().get(name)));

                    if (name.equals(PowerType.TYPE_KEY)) {
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
