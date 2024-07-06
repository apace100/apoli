package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class PreventBlockSelectionPower extends Power {

    private final Predicate<CachedBlockPosition> blockCondition;

    public PreventBlockSelectionPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> blockCondition) {
        super(type, entity);
        this.blockCondition = blockCondition;
    }

    public boolean doesPrevent(BlockPos pos) {
        return blockCondition == null
            || blockCondition.test(new CachedBlockPosition(entity.getWorld(), pos, true));
    }

    public static boolean doesPrevent(ShapeContext context, BlockPos pos) {
        return context instanceof EntityShapeContext entityContext
            && PowerHolderComponent.hasPower(entityContext.getEntity(), PreventBlockSelectionPower.class, p -> p.doesPrevent(pos));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_block_selection"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
            data -> (type, entity) -> new PreventBlockSelectionPower(
                type,
                entity,
                data.get("block_condition")
            )
        ).allowCondition();
    }
}
