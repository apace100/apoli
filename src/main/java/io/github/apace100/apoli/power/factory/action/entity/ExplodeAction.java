package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.function.Predicate;

public class ExplodeAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        World world = entity.getWorld();
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
            entity,
            entity.getPos(),
            data.getFloat("power"),
            data.getBoolean("create_fire"),
            data.get("destruction_type"),
            MiscUtil.getExplosionBehavior(world, data.get("indestructible_resistance"), indestructibleCondition)
        );

    }
    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("explode"),
            new SerializableData()
                .add("power", SerializableDataTypes.FLOAT)
                .add("destruction_type", ApoliDataTypes.BACKWARDS_COMPATIBLE_DESTRUCTION_TYPE, Explosion.DestructionType.DESTROY)
                .add("damage_self", SerializableDataTypes.BOOLEAN, true)
                .add("indestructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("indestructible_resistance", SerializableDataTypes.FLOAT, 10.0f)
                .add("destructible", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("create_fire", SerializableDataTypes.BOOLEAN, false),
            ExplodeAction::action
        );
    }
}
