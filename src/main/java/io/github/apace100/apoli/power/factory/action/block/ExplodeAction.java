package io.github.apace100.apoli.power.factory.action.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Predicate;

public class ExplodeAction {

    public static void action(SerializableData.Instance data, Triple<World, BlockPos, Direction> block) {

        World world = block.getLeft();
        if (world.isClient) {
            return;
        }

        Predicate<CachedBlockPosition> indestructibleCondition = data.get("indestructible");
        if (data.isPresent("destructible")) {
            Predicate<CachedBlockPosition> destructibleCondition = data.get("destructible");
            indestructibleCondition = MiscUtil.combineOr(destructibleCondition.negate(), indestructibleCondition);
        }

        MiscUtil.createExplosion(
            world,
            Vec3d.ofCenter(block.getMiddle()),
            data.getFloat("power"),
            data.get("create_fire"),
            data.get("destruction_type"),
            MiscUtil.getExplosionBehavior(world, data.get("indestructible_resistance"), indestructibleCondition)
        );

    }

    public static ActionFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("explode"),
            new SerializableData()
                .add("power", SerializableDataTypes.FLOAT)
                .add("destruction_type", ApoliDataTypes.BACKWARDS_COMPATIBLE_DESTRUCTION_TYPE, Explosion.DestructionType.DESTROY)
                .add("indestructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("indestructible_resistance", SerializableDataTypes.FLOAT, 10.0f)
                .add("destructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("create_fire", SerializableDataTypes.BOOLEAN, false),
            ExplodeAction::action
        );
    }
}
