package io.github.apace100.apoli.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AreaOfEffectAction {
    public static void action(SerializableData.Instance data, Entity entity) {
        List<Consumer<Entity>> actions = new ArrayList<>();

        if (data.isPresent("action")) {
            actions.add((Consumer<Entity>) data.get("action"));
        }

        if (data.isPresent("actions")) {
            actions.addAll((Collection<? extends Consumer<Entity>>) data.get("actions"));
        }

        Predicate<Entity> predicate = data.isPresent("condition") ? (Predicate<Entity>) data.get("condition") : x -> true;
        Predicate<Pair<Entity, Entity>> bi_predicate = data.isPresent("bi_condition") ? (Predicate<Pair<Entity, Entity>>) data.get("condition") : x -> true;
        boolean includeTarget = (boolean) data.get("include_target");
        double radius = (double) data.get("radius");
        double diameter = radius * 2;

        for (Entity check : entity.world.getNonSpectatingEntities(Entity.class, Box.of(entity.getLerpedPos(1F), diameter, diameter, diameter))) {
            if (check == entity && !includeTarget)
                continue;
            if (predicate.test(check) && bi_predicate.test(new Pair<>(entity, check)) && check.squaredDistanceTo(entity) < radius * radius)
                actions.forEach(x -> x.accept(check));
        }
    }

    public static ActionFactory<Entity> createFactory() {
        return new ActionFactory<Entity>(Apoli.identifier("area_of_effect"),
                new SerializableData()
                        .add("radius", SerializableDataTypes.DOUBLE, 16D)
                        .add("action", ApoliDataTypes.ENTITY_ACTION, null)
                        .add("actions", ApoliDataTypes.ENTITY_ACTIONS, null)
                        .add("condition", ApoliDataTypes.ENTITY_CONDITION, null)
                        .add("bi_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("include_target", SerializableDataTypes.BOOLEAN, false),
                AreaOfEffectAction::action
        );
    }
}

