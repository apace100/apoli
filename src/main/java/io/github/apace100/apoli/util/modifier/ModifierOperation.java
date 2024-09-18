package io.github.apace100.apoli.util.modifier;

import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.type.CooldownPowerType;
import io.github.apace100.apoli.power.type.VariableIntPowerType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.data.client.BlockStateVariantMap;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public enum ModifierOperation implements IModifierOperation {

    ADD_BASE_EARLY(Phase.BASE, 0, (values, base, current) -> base + values.stream().reduce(0.0, Double::sum)),
    MULTIPLY_BASE_ADDITIVE(Phase.BASE, 100, (values, base, current) ->
        current + (base * values.stream().reduce(0.0, Double::sum))),
    MULTIPLY_BASE_MULTIPLICATIVE(Phase.BASE, 200, (values, base, current) -> {
        double value = current;
        for(double v : values) {
            value *= (1 + v);
        }
        return value;
    }),
    ADD_BASE_LATE(Phase.BASE, 300, (values, base, current) -> {
        double value = current;
        for(double v : values) {
            value += v;
        }
        return value;
    }),
    MIN_BASE(Phase.BASE, 400, (values, base, current) -> {
        double value = current;
        for(double v : values) {
            value = Math.max(v, value);
        }
        return value;
    }),
    MAX_BASE(Phase.BASE, 500, (values, base, current) -> {
        double value = current;
        for(double v : values) {
            value = Math.min(v, value);
        }
        return value;
    }),
    SET_BASE(Phase.BASE, 600, (values, base, current) -> {
        double value = current;
        for(double v : values) {
            value = v;
        }
        return value;
    }),
    MULTIPLY_TOTAL_ADDITIVE(Phase.TOTAL, 0, (values, base, current) ->
        current + (base * values.stream().reduce(0.0, Double::sum))),
    MULTIPLY_TOTAL_MULTIPLICATIVE(Phase.TOTAL, 100, (values, base, current) -> {
        double value = current;
        for(double v : values) {
            value *= (1 + v);
        }
        return value;
    }),
    ADD_TOTAL_LATE(Phase.TOTAL, 200, (values, base, current) -> {
        double value = current;
        for(double v : values) {
            value = v;
        }
        return value;
    }),
    MIN_TOTAL(Phase.TOTAL, 300, (values, base, current) -> {
        double value = current;
        for(double v : values) {
            value = Math.max(v, value);
        }
        return value;
    }),
    MAX_TOTAL(Phase.TOTAL, 400, (values, base, current) -> {
        double value = current;
        for(double v : values) {
            value = Math.min(v, value);
        }
        return value;
    }),
    SET_TOTAL(Phase.TOTAL, 500, (values, base, current) -> {
        double value = current;
        for(double v : values) {
            value = v;
        }
        return value;
    });

    public static final SerializableData DATA = new SerializableData()
        .add("amount", SerializableDataTypes.DOUBLE, null)
        .add("resource", ApoliDataTypes.POWER_REFERENCE, null)
        .add("modifier", Modifier.LIST_TYPE, null)
        .validate(data -> {

            if (!data.isPresent("amount") && !data.isPresent("resource")) {
                return DataResult.error(() -> "Any of \"amount\" and \"resource\" fields must be defined!");
            }

            else {
                return DataResult.success(data);
            }

        });

    private final BlockStateVariantMap.TriFunction<Collection<Double>, Double, Double, Double> function;
    private final Phase phase;
    private final int order;

    ModifierOperation(Phase phase, int order, BlockStateVariantMap.TriFunction<Collection<Double>, Double, Double, Double> function) {
        this.phase = phase;
        this.order = order;
        this.function = function;
    }

    @Override
    public Phase getPhase() {
        return phase;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public SerializableData getSerializableData() {
        return DATA;
    }

    @Override
    public double apply(Entity entity, List<SerializableData.Instance> dataList, double base, double current) {

        Stream<Double> values = dataList.stream().map(data -> {

            Collection<Modifier> modifiers = data.getOrElseGet("modifier", ArrayList::new);
            double amount = data.getOrElseGet("amount", () -> {

                PowerReference resource = data.get("resource");
                Integer resourceValue = switch (resource.getType(entity)) {
                    case VariableIntPowerType varInt ->
                        varInt.getValue();
                    case CooldownPowerType cooldown ->
                        cooldown.getRemainingTicks();
                    case null, default ->
                        0;
                };

                return resourceValue.doubleValue();

            });

            return ModifierUtil.applyModifiers(entity, modifiers, amount);

        });

        return function.apply(values.toList(), base, current);

    }

}
