package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.bientity.OwnerCondition;
import io.github.apace100.apoli.power.factory.condition.bientity.RelativeRotationCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.function.Predicate;

public class BiEntityConditions {

    public static void register() {
        MetaConditions.register(ApoliDataTypes.BIENTITY_CONDITION, BiEntityConditions::register);
        register(new ConditionFactory<>(Apoli.identifier("invert"), new SerializableData()
            .add("condition", ApoliDataTypes.BIENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Pair<Entity, Entity>> cond = data.get("condition");
                return cond.test(new Pair<>(pair.getRight(), pair.getLeft()));
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("actor_condition"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = data.get("condition");
                return cond.test(pair.getLeft());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("target_condition"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = data.get("condition");
                return cond.test(pair.getRight());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("either"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = data.get("condition");
                return cond.test(pair.getLeft()) || cond.test(pair.getRight());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("both"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = data.get("condition");
                return cond.test(pair.getLeft()) && cond.test(pair.getRight());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("undirected"), new SerializableData()
            .add("condition", ApoliDataTypes.BIENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Pair<Entity, Entity>> cond = data.get("condition");
                return cond.test(pair) || cond.test(new Pair<>(pair.getRight(), pair.getLeft()));
            }
            ));

        register(new ConditionFactory<>(Apoli.identifier("distance"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, pair) -> {
                double distanceSq = pair.getLeft().getPos().squaredDistanceTo(pair.getRight().getPos());
                double comp = data.getDouble("compare_to");
                comp *= comp;
                return ((Comparison)data.get("comparison")).compare(distanceSq, comp);
            }
            ));
        register(new ConditionFactory<>(Apoli.identifier("can_see"), new SerializableData()
            .add("shape_type", SerializableDataType.enumValue(RaycastContext.ShapeType.class), RaycastContext.ShapeType.VISUAL)
            .add("fluid_handling", SerializableDataType.enumValue(RaycastContext.FluidHandling.class), RaycastContext.FluidHandling.NONE),
            (data, pair) -> {
                RaycastContext.ShapeType shapeType = data.get("shape_type");
                RaycastContext.FluidHandling fluidHandling = data.get("fluid_handling");
                if (pair.getRight().getWorld() != pair.getLeft().getWorld()) {
                    return false;
                } else {
                    Vec3d vec3d = new Vec3d(pair.getLeft().getX(), pair.getLeft().getEyeY(), pair.getLeft().getZ());
                    Vec3d vec3d2 = new Vec3d(pair.getRight().getX(), pair.getRight().getEyeY(), pair.getRight().getZ());
                    if (vec3d2.distanceTo(vec3d) > 128.0D) {
                        return false;
                    } else {
                        return pair.getLeft().getWorld().raycast(new RaycastContext(vec3d, vec3d2, shapeType, fluidHandling, pair.getLeft())).getType() == HitResult.Type.MISS;
                    }
                }
            }
        ));
        register(OwnerCondition.getFactory());
        register(new ConditionFactory<>(Apoli.identifier("riding"), new SerializableData(),
            (data, pair) -> pair.getLeft().getVehicle() == pair.getRight()
        ));
        register(new ConditionFactory<>(Apoli.identifier("riding_root"), new SerializableData(),
            (data, pair) -> pair.getLeft().getRootVehicle() == pair.getRight()
        ));
        register(new ConditionFactory<>(Apoli.identifier("riding_recursive"), new SerializableData(),
            (data, pair) -> {
                if(pair.getLeft().getVehicle() == null) {
                    return false;
                }
                Entity vehicle = pair.getLeft().getVehicle();
                while(vehicle != pair.getRight() && vehicle != null) {
                    vehicle = vehicle.getVehicle();
                }
                return vehicle == pair.getRight();
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("attack_target"), new SerializableData(),
            (data, pair) -> {
                if(pair.getLeft() instanceof MobEntity) {
                    return ((MobEntity)pair.getLeft()).getTarget() == pair.getRight();
                }
                if(pair.getLeft() instanceof Angerable) {
                    return ((Angerable)pair.getLeft()).getTarget() == pair.getRight();
                }
                return false;
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("attacker"), new SerializableData(),
            (data, pair) -> {
                if(pair.getRight() instanceof LivingEntity living) {
                    return living.getAttacker() == pair.getLeft();
                }
                return false;
            }
        ));
        register(RelativeRotationCondition.getFactory());
    }

    private static void register(ConditionFactory<Pair<Entity, Entity>> conditionFactory) {
        Registry.register(ApoliRegistries.BIENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
