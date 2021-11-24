package io.github.apace100.apoli.power.factory.action.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
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
        if(data.isPresent("indestructible")) {
            Predicate<CachedBlockPosition> blockCondition = data.get("indestructible");
            ExplosionBehavior eb = new ExplosionBehavior() {
                @Override
                public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
                    Optional<Float> def = super.getBlastResistance(explosion, world, pos, blockState, fluidState);
                    Optional<Float> ovr = blockCondition.test(
                        new CachedBlockPosition(block.getLeft(), pos, true)) ?
                        Optional.of(Blocks.WATER.getBlastResistance()) : Optional.empty();
                    return ovr.isPresent() ? def.isPresent() ? def.get() > ovr.get() ? def : ovr : ovr : def;
                }
            };
            block.getLeft().createExplosion(null,
                    DamageSource.explosion((LivingEntity) null),
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

    private static <T extends Comparable<T>> void modifyEnumState(World world, BlockPos pos, BlockState originalState, Property<T> property, String value) {
        Optional<T> enumValue = property.parse(value);
        enumValue.ifPresent(v -> world.setBlockState(pos, originalState.with(property, v)));
    }

    public static ActionFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionFactory<>(Apoli.identifier("explode"),
            new SerializableData()
                .add("power", SerializableDataTypes.FLOAT)
                .add("destruction_type", SerializableDataType.enumValue(Explosion.DestructionType.class), Explosion.DestructionType.BREAK)
                .add("damage_self", SerializableDataTypes.BOOLEAN, true)
                .add("indestructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("destructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("create_fire", SerializableDataTypes.BOOLEAN, false),
            ExplodeAction::action
        );
    }
}
