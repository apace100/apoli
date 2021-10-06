package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.function.Predicate;

public class PreventSleepPower extends Power {

    private final Predicate<CachedBlockPosition> predicate;
    private final String message;
    private final boolean allowSpawnPoint;

    public PreventSleepPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> predicate, String message, boolean allowSpawnPoint) {
        super(type, entity);
        this.predicate = predicate;
        this.message = message;
        this.allowSpawnPoint = allowSpawnPoint;
    }

    public boolean doesPrevent(WorldView world, BlockPos pos) {
        CachedBlockPosition cbp = new CachedBlockPosition(world, pos, true);
        return predicate.test(cbp);
    }

    public String getMessage() {
        return message;
    }

    public boolean doesAllowSpawnPoint() {
        return allowSpawnPoint;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_sleep"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("message", SerializableDataTypes.STRING, "origins.cant_sleep")
                .add("set_spawn_point", SerializableDataTypes.BOOLEAN, false),
            data ->
                (type, player) ->
                    new PreventSleepPower(type, player,
                        data.isPresent("block_condition") ? (ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition") : cbp -> true,
                        data.getString("message"), data.getBoolean("set_spawn_point")))
            .allowCondition();
    }
}
