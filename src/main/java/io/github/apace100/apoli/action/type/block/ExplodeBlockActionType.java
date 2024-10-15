package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.Optional;
import java.util.function.Predicate;

public class ExplodeBlockActionType extends BlockActionType {

    public static final DataObjectFactory<ExplodeBlockActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("destructible_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("indestructible_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("destruction_type", SerializableDataTypes.DESTRUCTION_TYPE, Explosion.DestructionType.DESTROY)
            .add("create_fire", SerializableDataTypes.BOOLEAN, false)
            .add("power", SerializableDataTypes.FLOAT)
            .add("indestructible_resistance", SerializableDataTypes.FLOAT, 10.0F),
        data -> new ExplodeBlockActionType(
            data.get("destructible_condition"),
            data.get("indestructible_condition"),
            data.get("destruction_type"),
            data.get("create_fire"),
            data.get("power"),
            data.get("indestructible_resistance")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("destructible_condition", actionType.destructibleCondition)
            .set("indestructible_condition", actionType.indestructibleCondition)
            .set("destruction_type", actionType.destructionType)
            .set("create_fire", actionType.createFire)
            .set("power", actionType.power)
            .set("indestructible_resistance", actionType.indestructibleResistance)
    );

    private final Optional<BlockCondition> destructibleCondition;
    private final Optional<BlockCondition> indestructibleCondition;

    private final Explosion.DestructionType destructionType;
    private final boolean createFire;

    private final float power;
    private final float indestructibleResistance;

    public ExplodeBlockActionType(Optional<BlockCondition> destructibleCondition, Optional<BlockCondition> indestructibleCondition, Explosion.DestructionType destructionType, boolean createFire, float power, float indestructibleResistance) {
        this.destructibleCondition = destructibleCondition;
        this.indestructibleCondition = indestructibleCondition;
        this.destructionType = destructionType;
        this.createFire = createFire;
        this.power = power;
        this.indestructibleResistance = indestructibleResistance;
    }

    @Override
    public void execute(World world, BlockPos pos, Optional<Direction> direction) {

        if (world.isClient()) {
            return;
        }

        Predicate<BlockConditionContext> behaviorCondition = indestructibleCondition.orElse(null);
        if (destructibleCondition.isPresent()) {
            behaviorCondition = MiscUtil.combineOr(destructibleCondition.get().negate(), behaviorCondition);
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
