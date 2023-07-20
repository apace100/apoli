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

import java.util.function.Predicate;

public class ModifyHarvestPower extends Power {

    private final Predicate<CachedBlockPosition> predicate;
    private boolean allow;

    public ModifyHarvestPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> predicate, boolean allow) {
        super(type, entity);
        this.predicate = predicate;
        this.allow = allow;
    }

    public boolean doesApply(BlockPos pos) {
        CachedBlockPosition cbp = new CachedBlockPosition(entity.getWorld(), pos, true);
        return predicate.test(cbp);
    }

    public boolean doesApply(CachedBlockPosition pos) {
        return predicate.test(pos);
    }

    public boolean isHarvestAllowed() {
        return allow;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_harvest"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("allow", SerializableDataTypes.BOOLEAN),
            data ->
                (type, player) ->
                    new ModifyHarvestPower(type, player,
                        data.isPresent("block_condition") ? (ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition") : cbp -> true,
                        data.getBoolean("allow")))
            .allowCondition();
    }
}
