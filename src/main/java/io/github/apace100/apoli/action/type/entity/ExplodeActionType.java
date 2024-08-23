package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class ExplodeActionType {

    public static void action(Entity entity, float power, Explosion.DestructionType destructionType, @Nullable Predicate<CachedBlockPosition> indestructibleCondition, float indestructibleResistance, @Nullable Predicate<CachedBlockPosition> destructibleCondition, boolean damageSelf, boolean createFire) {

        World world = entity.getWorld();
        if (world.isClient) {
            return;
        }

        if (destructibleCondition != null) {
            indestructibleCondition = MiscUtil.combineOr(destructibleCondition.negate(), indestructibleCondition);
        }

        Vec3d pos = entity.getPos();
        MiscUtil.createExplosion(
            world,
            damageSelf ? null : entity,
            Explosion.createDamageSource(world, entity),
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            power,
            createFire,
            destructionType,
            MiscUtil.getExplosionBehavior(world, indestructibleResistance, indestructibleCondition)
        );

    }
    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("explode"),
            new SerializableData()
                .add("power", SerializableDataTypes.FLOAT)
                .add("destruction_type", ApoliDataTypes.DESTRUCTION_TYPE, Explosion.DestructionType.DESTROY)
                .add("indestructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("indestructible_resistance", SerializableDataTypes.FLOAT, 10.0f)
                .add("destructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("damage_self", SerializableDataTypes.BOOLEAN, true)
                .add("create_fire", SerializableDataTypes.BOOLEAN, false),
            (data, entity) -> action(entity,
                data.get("power"),
                data.get("destruction_type"),
                data.get("indestructible"),
                data.get("indestructible_resistance"),
                data.get("destructible"),
                data.get("damage_self"),
                data.get("create_fire")
            )
        );
    }
}
