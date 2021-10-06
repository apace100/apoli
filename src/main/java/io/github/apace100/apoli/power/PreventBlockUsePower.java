package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.function.Predicate;

public class PreventBlockUsePower extends Power {

    private final Predicate<CachedBlockPosition> predicate;

    public PreventBlockUsePower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> predicate) {
        super(type, entity);
        this.predicate = predicate;
    }

    public boolean doesPrevent(WorldView world, BlockPos pos) {
        CachedBlockPosition cbp = new CachedBlockPosition(world, pos, true);
        return predicate.test(cbp);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_block_use"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
            data ->
                (type, player) -> new PreventBlockUsePower(type, player,
                    (ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition")))
            .allowCondition();
    }
}
