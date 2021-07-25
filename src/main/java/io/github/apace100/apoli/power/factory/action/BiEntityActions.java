package io.github.apace100.apoli.power.factory.action;


import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.FilterableWeightedList;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;
import java.util.Random;

public class BiEntityActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ActionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("actions", ApoliDataTypes.BIENTITY_ACTIONS),
            (data, entities) -> ((List<ActionFactory<Pair<Entity, Entity>>.Instance>)data.get("actions")).forEach((e) -> e.accept(entities))));
        register(new ActionFactory<>(Apoli.identifier("chance"), new SerializableData()
            .add("action", ApoliDataTypes.BIENTITY_ACTION)
            .add("chance", SerializableDataTypes.FLOAT),
            (data, entities) -> {
                if(new Random().nextFloat() < data.getFloat("chance")) {
                    ((ActionFactory<Pair<Entity, Entity>>.Instance)data.get("action")).accept(entities);
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("choice"), new SerializableData()
            .add("actions", SerializableDataType.weightedList(ApoliDataTypes.BIENTITY_ACTION)),
            (data, entities) -> {
                FilterableWeightedList<ActionFactory<Pair<Entity, Entity>>.Instance> actionList = (FilterableWeightedList<ActionFactory<Pair<Entity, Entity>>.Instance>)data.get("actions");
                ActionFactory<Pair<Entity, Entity>>.Instance action = actionList.pickRandom(new Random());
                action.accept(entities);
            }));
        register(new ActionFactory<>(Apoli.identifier("invert"), new SerializableData()
            .add("action", ApoliDataTypes.BIENTITY_ACTION),
            (data, entities) -> {
                ((ActionFactory<Pair<Entity, Entity>>.Instance)data.get("action")).accept(new Pair<>(entities.getRight(), entities.getLeft()));
            }));
        register(new ActionFactory<>(Apoli.identifier("actor_action"), new SerializableData()
            .add("action", ApoliDataTypes.ENTITY_ACTION),
            (data, entities) -> {
                ((ActionFactory<Entity>.Instance)data.get("action")).accept(entities.getLeft());
            }));
        register(new ActionFactory<>(Apoli.identifier("target_action"), new SerializableData()
            .add("action", ApoliDataTypes.ENTITY_ACTION),
            (data, entities) -> {
                ((ActionFactory<Entity>.Instance)data.get("action")).accept(entities.getRight());
            }));

        register(new ActionFactory<>(Apoli.identifier("mount"), new SerializableData(),
            (data, entities) -> {
                entities.getLeft().startRiding(entities.getRight(), true);
            }));
        register(new ActionFactory<>(Apoli.identifier("set_in_love"), new SerializableData(),
            (data, entities) -> {
                if(entities.getRight() instanceof AnimalEntity && entities.getLeft() instanceof PlayerEntity) {
                    ((AnimalEntity)entities.getRight()).lovePlayer((PlayerEntity)entities.getLeft());
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("tame"), new SerializableData(),
            (data, entities) -> {
                if(entities.getRight() instanceof TameableEntity && entities.getLeft() instanceof PlayerEntity) {
                    if(!((TameableEntity)entities.getRight()).isTamed()) {
                        ((TameableEntity)entities.getRight()).setOwner((PlayerEntity)entities.getLeft());
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("add_velocity"), new SerializableData()
            .add("x", SerializableDataTypes.FLOAT, 0F)
            .add("y", SerializableDataTypes.FLOAT, 0F)
            .add("z", SerializableDataTypes.FLOAT, 0F)
            .add("set", SerializableDataTypes.BOOLEAN, false),
            (data, entities) -> {
                Vec3f vec = new Vec3f(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"));
                Vec3d delta = entities.getRight().getPos().subtract(entities.getLeft().getPos()).normalize();
                TriConsumer<Float, Float, Float> method = entities.getRight()::addVelocity;
                if(data.getBoolean("set")) {
                    method = entities.getRight()::setVelocity;
                }
                Space.rotateVectorToBase(delta, vec);
                method.accept(vec.getX(), vec.getY(), vec.getZ());
            }));
    }

    private static void register(ActionFactory<Pair<Entity, Entity>> actionFactory) {
        Registry.register(ApoliRegistries.BIENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
