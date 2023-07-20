package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;

import java.util.Optional;
import java.util.function.Predicate;

public class ExplodeAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if(entity.getWorld().isClient) {
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
            ExplosionBehavior eb = getExplosionBehaviour(entity.getWorld(), indestructible);
            entity.getWorld().createExplosion(data.getBoolean("damage_self") ? null : entity,
                null,
                eb, entity.getX(), entity.getY(), entity.getZ(),
                data.getFloat("power"), data.getBoolean("create_fire"),
                data.get("destruction_type"));
        } else {
            explode(entity.getWorld(), data.getBoolean("damage_self") ? null : entity, null, null,
                entity.getX(), entity.getY(), entity.getZ(),
                data.getFloat("power"), data.getBoolean("create_fire"),
                data.get("destruction_type"));
        }
    }

    private static void explode(World world, Entity entity, DamageSource damageSource, ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, Explosion.DestructionType destructionType) {
        Explosion explosion = new Explosion(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType);
        explosion.collectBlocksAndDamageEntities();
        explosion.affectWorld(true);
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

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("explode"),
            new SerializableData()
                .add("power", SerializableDataTypes.FLOAT)
                .add("destruction_type", ApoliDataTypes.BACKWARDS_COMPATIBLE_DESTRUCTION_TYPE, Explosion.DestructionType.DESTROY)
                .add("damage_self", SerializableDataTypes.BOOLEAN, true)
                .add("indestructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("destructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("create_fire", SerializableDataTypes.BOOLEAN, false),
            ExplodeAction::action
        );
    }
}
