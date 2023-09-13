package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class ModifyHarvestPower extends Power implements Prioritized<ModifyHarvestPower>, Comparable<ModifyHarvestPower> {

    private final Predicate<CachedBlockPosition> blockCondition;

    private final boolean allow;
    private final int priority;

    public ModifyHarvestPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> predicate, boolean allow, int priority) {
        super(type, entity);
        this.blockCondition = predicate;
        this.allow = allow;
        this.priority = priority;
    }

    @Override
    public int compareTo(@NotNull ModifyHarvestPower other) {
        int priorityResult = Integer.compare(this.getPriority(), other.getPriority());
        return priorityResult != 0 ? priorityResult : Boolean.compare(this.isHarvestAllowed(), other.isHarvestAllowed());
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean doesApply(BlockPos pos) {
        return doesApply(new CachedBlockPosition(entity.getWorld(), pos, true));
    }

    public boolean doesApply(CachedBlockPosition pos) {
        return blockCondition == null || blockCondition.test(pos);
    }

    public boolean isHarvestAllowed() {
        return allow;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("modify_harvest"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("allow", SerializableDataTypes.BOOLEAN)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (powerType, livingEntity) -> new ModifyHarvestPower(
                powerType,
                livingEntity,
                data.get("block_condition"),
                data.get("allow"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
