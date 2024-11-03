package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PreventSleepPowerType extends PowerType implements Prioritized<PreventSleepPowerType>, Comparable<PreventSleepPowerType> {

    public static final TypedDataObjectFactory<PreventSleepPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("message", ApoliDataTypes.DEFAULT_TRANSLATABLE_TEXT, Text.translatable("text.apoli.cannot_sleep"))
            .add("set_spawn_point", SerializableDataTypes.BOOLEAN, true)
            .add("priority", SerializableDataTypes.INT, 0),
        (data, condition) -> new PreventSleepPowerType(
            data.get("block_condition"),
            data.get("message"),
            data.get("set_spawn_point"),
            data.get("priority"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("block_condition", powerType.blockCondition)
            .set("message", powerType.getMessage())
            .set("set_spawn_point", powerType.doesAllowSpawnPoint())
            .set("priority", powerType.getPriority())
    );

    private final Optional<BlockCondition> blockCondition;
    private final Text message;

    private final boolean allowSpawnPoint;
    private final int priority;

    public PreventSleepPowerType(Optional<BlockCondition> blockCondition, Text message, boolean allowSpawnPoint, int priority, Optional<EntityCondition> condition) {
        super(condition);
        this.blockCondition = blockCondition;
        this.message = message;
        this.allowSpawnPoint = allowSpawnPoint;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_SLEEP;
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

    public boolean doesPrevent(WorldView worldView, BlockPos pos) {
        return worldView instanceof World world && blockCondition
            .map(condition -> condition.test(world, pos))
            .orElse(true);
    }

    public Text getMessage() {
        return message;
    }

    public boolean doesAllowSpawnPoint() {
        return allowSpawnPoint;
    }

}
