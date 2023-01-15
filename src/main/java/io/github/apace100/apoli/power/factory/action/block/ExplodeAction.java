package io.github.apace100.apoli.power.factory.action.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Optional;
import java.util.function.Predicate;

public class ExplodeAction {

    public static void action(SerializableData.Instance data, Triple<World, BlockPos, Direction> block) {
        if(block.getLeft().isClient) {
            return;
        }

        Predicate<CachedBlockPosition> indestructible = null;
        if(data.isPresent("indestructible")) {
            indestructible = MiscUtil.combineOr(indestructible, data.get("indestructible"));
        }
        if(data.isPresent("destructible")) {
            Predicate<CachedBlockPosition> destructibleCondition = data.get("destructible");
            indestructible = MiscUtil.combineOr(indestructible, destructibleCondition.negate());
        }

        if(indestructible != null) {
            ExplosionBehavior eb = getExplosionBehaviour(block.getLeft(), indestructible);
            block.getLeft().createExplosion(null,
                DamageSource.explosion(null),
                eb, block.getMiddle().getX() + 0.5, block.getMiddle().getY() + 0.5, block.getMiddle().getZ() + 0.5,
                data.getFloat("power"), data.getBoolean("create_fire"),
                data.get("destruction_type"));
        } else {
            block.getLeft().createExplosion(null,
                block.getMiddle().getX() + 0.5, block.getMiddle().getY() + 0.5, block.getMiddle().getZ() + 0.5,
                data.getFloat("power"), data.getBoolean("create_fire"),
                data.get("destruction_type"));
        }
    }

    private static ExplosionBehavior getExplosionBehaviour(World world, Predicate<CachedBlockPosition> indestructiblePredicate) {
        return new ExplosionBehavior() {
            @Override
            public Optional<Float> getBlastResistance(Explosion explosion, BlockView blockView, BlockPos pos, BlockState blockState, FluidState fluidState) {
                CachedBlockPosition cbp = new CachedBlockPosition(world, pos, true);
                Optional<Float> def = super.getBlastResistance(explosion, world, pos, blockState, fluidState);
                Optional<Float> ovr = indestructiblePredicate.test(cbp) ?
                    Optional.of(Blocks.WATER.getBlastResistance()) : Optional.empty();
                return ovr.isPresent() ? def.isPresent() ? def.get() > ovr.get() ? def : ovr : ovr : def;
            }
        };
    }

    public static ActionFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionFactory<>(Apoli.identifier("explode"),
            new SerializableData()
                .add("power", SerializableDataTypes.FLOAT)
                .add("destruction_type", SerializableDataType.enumValue(Explosion.DestructionType.class), Explosion.DestructionType.DESTROY)
                .add("indestructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("destructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("create_fire", SerializableDataTypes.BOOLEAN, false),
            ExplodeAction::action
        );
    }
}
