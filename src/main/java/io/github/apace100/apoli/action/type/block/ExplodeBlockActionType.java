package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.Optional;
import java.util.function.Predicate;

public class ExplodeBlockActionType extends BlockActionType {

    public static final TypedDataObjectFactory<ExplodeBlockActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("destructible", BlockCondition.DATA_TYPE, null)
            .add("indestructible", BlockCondition.DATA_TYPE, null)
            .add("destruction_type", SerializableDataTypes.DESTRUCTION_TYPE, Explosion.DestructionType.DESTROY)
            .add("create_fire", SerializableDataTypes.BOOLEAN, false)
            .add("power", SerializableDataTypes.NON_NEGATIVE_FLOAT)
            .add("indestructible_resistance", SerializableDataTypes.NON_NEGATIVE_FLOAT, 10.0F),
        data -> new ExplodeBlockActionType(
            data.get("destructible"),
            data.get("indestructible"),
            data.get("destruction_type"),
            data.get("create_fire"),
            data.get("power"),
            data.get("indestructible_resistance")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("destructible", actionType.destructibleCondition)
            .set("indestructible", actionType.indestructibleCondition)
            .set("destruction_type", actionType.destructionType)
            .set("create_fire", actionType.createFire)
            .set("power", actionType.power)
            .set("indestructible_resistance", actionType.indestructibleResistance)
    );

    private final BlockCondition destructibleCondition;
    private final BlockCondition indestructibleCondition;

    private final Explosion.DestructionType destructionType;
    private final boolean createFire;

    private final float power;
    private final float indestructibleResistance;

    public ExplodeBlockActionType(BlockCondition destructibleCondition, BlockCondition indestructibleCondition, Explosion.DestructionType destructionType, boolean createFire, float power, float indestructibleResistance) {
        this.destructibleCondition = destructibleCondition;
        this.indestructibleCondition = indestructibleCondition;
        this.destructionType = destructionType;
        this.createFire = createFire;
        this.power = power;
        this.indestructibleResistance = indestructibleResistance;
    }

    @Override
	protected void execute(World world, BlockPos pos, Optional<Direction> direction) {

        if (world.isClient()) {
            return;
        }

        Predicate<BlockConditionContext> behaviorCondition = indestructibleCondition;
        if (destructibleCondition != null) {
            behaviorCondition = MiscUtil.combineOr(destructibleCondition.negate(), behaviorCondition);
        }

        MiscUtil.createExplosion(
            world,
            pos.toCenterPos(),
            power,
            createFire,
            destructionType,
            MiscUtil.createExplosionBehavior(behaviorCondition, indestructibleResistance)
        );

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BlockActionTypes.EXPLODE;
    }

}
