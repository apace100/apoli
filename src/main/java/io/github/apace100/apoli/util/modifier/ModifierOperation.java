package io.github.apace100.apoli.util.modifier;

import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.util.PowerUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public enum ModifierOperation implements IModifierOperation {

    ADD_BASE_EARLY(Phase.BASE, 0,
        (values, base, baseTotal) -> values.reduce(baseTotal, Double::sum)),
    MULTIPLY_BASE_ADDITIVE(Phase.BASE, 100,
        (values, base, baseTotal) -> baseTotal + (base * values.reduce(0.0, Double::sum))),
    MULTIPLY_BASE_MULTIPLICATIVE(Phase.BASE, 200,
        (values, base, baseTotal) -> baseTotal * (1.0 + values.reduce(0.0, Double::sum))),
    ADD_BASE_LATE(Phase.BASE, 300,
        (values, base, baseTotal) -> values.reduce(baseTotal, Double::sum)),
    MIN_BASE(Phase.BASE, 400,
        (values, base, baseTotal) -> values.reduce(baseTotal, Math::max)),
    MAX_BASE(Phase.BASE, 500,
        (values, base, baseTotal) -> values.reduce(baseTotal, Math::min)),
    SET_BASE(Phase.BASE, 600,
        (values, base, baseTotal) -> values.reduce(baseTotal, (a, b) -> b)),

    ADD_TOTAL_EARLY(Phase.TOTAL, 0,
        (values, totalBase, total) -> values.reduce(total, Double::sum)),
    MULTIPLY_TOTAL_ADDITIVE(Phase.TOTAL, 100,
        (values, totalBase, total) -> total + (totalBase * values.reduce(0.0, Double::sum))),
    MULTIPLY_TOTAL_MULTIPLICATIVE(Phase.TOTAL, 200,
        (values, totalBase, total) -> total * (1.0 + values.reduce(0.0, Double::sum))),
    ADD_TOTAL_LATE(Phase.TOTAL, 300,
        (values, totalBase, total) -> values.reduce(total, Double::sum)),
    MIN_TOTAL(Phase.TOTAL, 400,
        (values, totalBase, total) -> values.reduce(total, Math::max)),
    MAX_TOTAL(Phase.TOTAL, 500,
        (values, totalBase, total) -> values.reduce(total, Math::min)),
    SET_TOTAL(Phase.TOTAL, 600,
        (values, totalBase, total) -> values.reduce(total, (a, b) -> b));

    public static final SerializableData DATA = new SerializableData()
        .add("amount", SerializableDataTypes.DOUBLE, null)
        .add("resource", ApoliDataTypes.RESOURCE_REFERENCE, null)
        .add("modifier", Modifier.LIST_TYPE, null)
        .validate(data -> {

            if (!data.isPresent("amount") && !data.isPresent("resource")) {
                return DataResult.error(() -> "Any of \"amount\" and \"resource\" fields must be defined!");
            }

            else {
                return DataResult.success(data);
            }

        });

    private final TriFunction<Stream<Double>, Double, Double, Double> function;
    private final Phase phase;
    private final int order;

    ModifierOperation(Phase phase, int order, TriFunction<Stream<Double>, Double, Double, Double> function) {
        this.phase = phase;
        this.order = order;
        this.function = function;
    }

    @Override
    public SerializableData getSerializableData() {
        return DATA;
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
    public double apply(Entity entity, Collection<SerializableData.Instance> dataList, double base, double current) {

        Stream<Double> values = dataList.stream().map(data -> {

            Collection<Modifier> modifiers = data.getOrElseGet("modifier", ObjectArrayList::new);
            Optional<PowerReference> resource = Optional.ofNullable(data.get("resource"));

            double amount = resource
                .map(powerReference -> PowerUtil.getResourceValue(powerReference.getType(entity)))
                .orElseGet(() -> data.get("amount"));

            return ModifierUtil.applyModifiers(entity, modifiers, amount);

        });

        return function.apply(values, base, current);

    }

}
