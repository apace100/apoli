package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.function.Predicate;

public class PreventBlockUsePower extends Power {

    private final Predicate<CachedBlockPosition> blockCondition;

    public PreventBlockUsePower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> blockCondition) {
        super(type, entity);
        this.blockCondition = blockCondition;
    }

    public boolean doesPrevent(WorldView world, BlockPos pos) {
        return blockCondition == null || blockCondition.test(new CachedBlockPosition(world, pos, true));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("prevent_block_use"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
            data -> (powerType, livingEntity) -> new PreventBlockUsePower(
                powerType,
                livingEntity,
                data.get("block_condition")
            )
        ).allowCondition();
    }

}
