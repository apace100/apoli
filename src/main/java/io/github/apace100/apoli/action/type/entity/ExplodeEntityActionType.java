package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;

import java.util.function.Predicate;

public class ExplodeEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ExplodeEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("destructible", BlockCondition.DATA_TYPE, null)
            .add("indestructible", BlockCondition.DATA_TYPE, null)
            .add("destruction_type", SerializableDataTypes.DESTRUCTION_TYPE, Explosion.DestructionType.DESTROY)
            .add("damage_self", SerializableDataTypes.BOOLEAN, true)
            .add("create_fire", SerializableDataTypes.BOOLEAN, false)
            .add("power", SerializableDataTypes.NON_NEGATIVE_FLOAT)
            .add("indestructible_resistance", SerializableDataTypes.NON_NEGATIVE_FLOAT, 10.0F),
        data -> new ExplodeEntityActionType(
            data.get("destructible"),
            data.get("indestructible"),
            data.get("destruction_type"),
            data.get("damage_self"),
            data.get("create_fire"),
            data.get("power"),
            data.get("indestructible_resistance")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("destructible", actionType.destructibleCondition)
            .set("indestructible", actionType.indestructibleCondition)
            .set("destruction_type", actionType.destructionType)
            .set("damage_self", actionType.damageSelf)
            .set("create_fire", actionType.createFire)
            .set("power", actionType.power)
            .set("indestructible_resistance", actionType.indestructibleResistance)
    );

    private final BlockCondition destructibleCondition;
    private final BlockCondition indestructibleCondition;

    private final Explosion.DestructionType destructionType;

    private final boolean damageSelf;
    private final boolean createFire;

    private final float power;
    private final float indestructibleResistance;

    public ExplodeEntityActionType(BlockCondition destructibleCondition, BlockCondition indestructibleCondition, Explosion.DestructionType destructionType, boolean damageSelf, boolean createFire, float power, float indestructibleResistance) {
        this.destructibleCondition = destructibleCondition;
        this.indestructibleCondition = indestructibleCondition;
        this.destructionType = destructionType;
        this.damageSelf = damageSelf;
        this.createFire = createFire;
        this.power = power;
        this.indestructibleResistance = indestructibleResistance;
    }

    @Override
    protected void execute(Entity entity) {

        if (entity.getWorld().isClient()) {
            return;
        }

        Vec3d entityPos = entity.getPos();
        Predicate<BlockConditionContext> behaviorCondition = indestructibleCondition;

        if (destructibleCondition != null) {
            behaviorCondition = MiscUtil.combineOr(destructibleCondition.negate(), behaviorCondition);
        }

        MiscUtil.createExplosion(
            entity.getWorld(),
            damageSelf ? null : entity,
            Explosion.createDamageSource(entity.getWorld(), entity),
            entityPos.getX(),
            entityPos.getY(),
            entityPos.getZ(),
            power,
            createFire,
            destructionType,
            MiscUtil.createExplosionBehavior(behaviorCondition, indestructibleResistance)
        );

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.EXPLODE;
    }

}
