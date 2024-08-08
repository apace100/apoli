package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class PreventSleepPowerType extends PowerType implements Prioritized<PreventSleepPowerType>, Comparable<PreventSleepPowerType> {

    private final Predicate<CachedBlockPosition> blockCondition;
    private final Text message;

    private final boolean allowSpawnPoint;
    private final int priority;

    public PreventSleepPowerType(Power power, LivingEntity entity, Predicate<CachedBlockPosition> blockCondition, Text message, boolean allowSpawnPoint, int priority) {
        super(power, entity);
        this.blockCondition = blockCondition;
        this.message = message;
        this.allowSpawnPoint = allowSpawnPoint;
        this.priority = priority;
    }

    @Override
    public int compareTo(@NotNull PreventSleepPowerType other) {
        int cmp = Boolean.compare(this.doesAllowSpawnPoint(), other.doesAllowSpawnPoint());
        return cmp != 0 ? cmp : Integer.compare(this.getPriority(), other.getPriority());
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean doesPrevent(WorldView world, BlockPos pos) {
        return blockCondition == null
            || blockCondition.test(new CachedBlockPosition(world, pos, true));
    }

    public Text getMessage() {
        return message;
    }

    public boolean doesAllowSpawnPoint() {
        return allowSpawnPoint;
    }

    public static PowerTypeFactory<PreventSleepPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("prevent_sleep"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("message", ApoliDataTypes.DEFAULT_TRANSLATABLE_TEXT, Text.translatable("text.apoli.cannot_sleep"))
                .add("set_spawn_point", SerializableDataTypes.BOOLEAN, false)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new PreventSleepPowerType(power, entity,
                data.get("block_condition"),
                data.get("message"),
                data.get("set_spawn_point"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
