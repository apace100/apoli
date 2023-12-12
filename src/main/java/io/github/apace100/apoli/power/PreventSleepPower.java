package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class PreventSleepPower extends Power implements Prioritized<PreventSleepPower>, Comparable<PreventSleepPower> {

    private final Predicate<CachedBlockPosition> blockCondition;
    private final Text message;

    private final boolean allowSpawnPoint;
    private final int priority;

    public PreventSleepPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> blockCondition, Text message, boolean allowSpawnPoint, int priority) {
        super(type, entity);
        this.blockCondition = blockCondition;
        this.message = message;
        this.allowSpawnPoint = allowSpawnPoint;
        this.priority = priority;
    }

    @Override
    public int compareTo(@NotNull PreventSleepPower other) {
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

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("prevent_sleep"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("message", ApoliDataTypes.DEFAULT_TRANSLATABLE_TEXT, Text.translatable("text.apoli.cannot_sleep"))
                .add("set_spawn_point", SerializableDataTypes.BOOLEAN, false)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (powerType, livingEntity) -> new PreventSleepPower(
                powerType,
                livingEntity,
                data.get("block_condition"),
                data.get("message"),
                data.get("set_spawn_point"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
