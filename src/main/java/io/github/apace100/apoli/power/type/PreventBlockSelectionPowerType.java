package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

//  TODO: Expand the functionality scope of this power type -eggohito
public class PreventBlockSelectionPowerType extends PowerType {

    public static final TypedDataObjectFactory<PreventBlockSelectionPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, condition) -> new PreventBlockSelectionPowerType(
            data.get("block_condition"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("block_condition", powerType.blockCondition)
    );

    private final Optional<BlockCondition> blockCondition;

    public PreventBlockSelectionPowerType(Optional<BlockCondition> blockCondition, Optional<EntityCondition> condition) {
        super(condition);
        this.blockCondition = blockCondition;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_BLOCK_SELECTION;
    }

    public boolean doesPrevent(BlockPos pos) {
        return blockCondition
            .map(condition -> condition.test(getHolder().getWorld(), pos))
            .orElse(true);
    }

    public static boolean doesPrevent(ShapeContext context, BlockPos pos) {
        return context instanceof EntityShapeContext entityContext
            && PowerHolderComponent.hasPowerType(entityContext.getEntity(), PreventBlockSelectionPowerType.class, p -> p.doesPrevent(pos));
    }

}
