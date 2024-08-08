package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class ModifyHarvestPowerType extends PowerType implements Prioritized<ModifyHarvestPowerType>, Comparable<ModifyHarvestPowerType> {

    private final Predicate<CachedBlockPosition> blockCondition;

    private final boolean allow;
    private final int priority;

    public ModifyHarvestPowerType(Power power, LivingEntity entity, Predicate<CachedBlockPosition> predicate, boolean allow, int priority) {
        super(power, entity);
        this.blockCondition = predicate;
        this.allow = allow;
        this.priority = priority;
    }

    @Override
    public int compareTo(@NotNull ModifyHarvestPowerType other) {
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

    public static PowerTypeFactory<ModifyHarvestPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_harvest"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("allow", SerializableDataTypes.BOOLEAN)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new ModifyHarvestPowerType(power, entity,
                data.get("block_condition"),
                data.get("allow"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
