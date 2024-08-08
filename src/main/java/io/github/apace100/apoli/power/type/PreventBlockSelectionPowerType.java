package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class PreventBlockSelectionPowerType extends PowerType {

    private final Predicate<CachedBlockPosition> blockCondition;

    public PreventBlockSelectionPowerType(Power power, LivingEntity entity, Predicate<CachedBlockPosition> blockCondition) {
        super(power, entity);
        this.blockCondition = blockCondition;
    }

    public boolean doesPrevent(BlockPos pos) {
        return blockCondition == null
            || blockCondition.test(new CachedBlockPosition(entity.getWorld(), pos, true));
    }

    public static boolean doesPrevent(ShapeContext context, BlockPos pos) {
        return context instanceof EntityShapeContext entityContext
            && PowerHolderComponent.hasPowerType(entityContext.getEntity(), PreventBlockSelectionPowerType.class, p -> p.doesPrevent(pos));
    }

    public static PowerTypeFactory<PreventBlockSelectionPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("prevent_block_selection"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
            data -> (power, entity) -> new PreventBlockSelectionPowerType(power, entity,
                data.get("block_condition")
            )
        ).allowCondition();
    }
}
