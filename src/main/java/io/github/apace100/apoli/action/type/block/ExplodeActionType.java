package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class ExplodeActionType {

    public static void action(World world, BlockPos pos, float power, Explosion.DestructionType destructionType, @Nullable Predicate<CachedBlockPosition> indestructibleCondition, float indestructibleResistance, @Nullable Predicate<CachedBlockPosition> destructibleCondition, boolean createFire) {

        if (world.isClient) {
            return;
        }

        if (destructibleCondition != null) {
            indestructibleCondition = MiscUtil.combineOr(destructibleCondition.negate(), indestructibleCondition);
        }

        MiscUtil.createExplosion(
            world,
            pos.toCenterPos(),
            power,
            createFire,
            destructionType,
            MiscUtil.getExplosionBehavior(world, indestructibleResistance, indestructibleCondition)
        );

    }

    public static ActionTypeFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("explode"),
            new SerializableData()
                .add("power", SerializableDataTypes.FLOAT)
                .add("destruction_type", ApoliDataTypes.DESTRUCTION_TYPE, Explosion.DestructionType.DESTROY)
                .add("indestructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("indestructible_resistance", SerializableDataTypes.FLOAT, 10.0f)
                .add("destructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("create_fire", SerializableDataTypes.BOOLEAN, false),
            (data, block) -> action(block.getLeft(), block.getMiddle(),
                data.get("power"),
                data.get("destruction_type"),
                data.get("indestructible"),
                data.get("indestructible_resistance"),
                data.get("destructible"),
                data.get("create_fire")
            )
        );
    }

}
