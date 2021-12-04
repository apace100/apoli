package io.github.apace100.apoli.power.factory.action.meta;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.function.Function;

public class IfElseListAction {

    public static <T, U> void action(SerializableData.Instance data, T t, Function<T, U> actionToConditionTypeFunction) {
        List<Pair<ConditionFactory<U>.Instance, ActionFactory<T>.Instance>> actions =
            data.get("actions");
        U u = actionToConditionTypeFunction.apply(t);
        for (Pair<ConditionFactory<U>.Instance, ActionFactory<T>.Instance> action: actions) {
            if(action.getLeft().test(u)) {
                action.getRight().accept(t);
                break;
            }
        }
    }

    public static <T, U> ActionFactory<T> getFactory(
        SerializableDataType<ActionFactory<T>.Instance> actionDataType,
        SerializableDataType<ConditionFactory<U>.Instance> conditionDataType,
        Function<T, U> actionToConditionTypeFunction) {
        return new ActionFactory<>(Apoli.identifier("if_else_list"), new SerializableData()
            .add("actions", SerializableDataType.list(SerializableDataType.compound(ClassUtil.castClass(Pair.class), new SerializableData()
                    .add("action", actionDataType)
                    .add("condition", conditionDataType),
                inst -> new Pair<ConditionFactory<U>.Instance, ActionFactory<T>.Instance>(
                    inst.get("condition"),
                    inst.get("action")),
                (data, pair) -> {
                    SerializableData.Instance inst = data.new Instance();
                    inst.set("condition", pair.getLeft());
                    inst.set("action", pair.getRight());
                    return inst;
                }))),
            (inst, t) -> action(inst, t, actionToConditionTypeFunction));
    }

    public static <T> ActionFactory<T> getFactory(
        SerializableDataType<ActionFactory<T>.Instance> actionDataType,
        SerializableDataType<ConditionFactory<T>.Instance> conditionDataType) {
        return getFactory(actionDataType, conditionDataType, t -> t);
    }
}
