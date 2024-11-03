package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.type.entity.SneakingEntityConditionType;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PhasingPowerType extends PowerType {

    public static final TypedDataObjectFactory<PhasingPowerType> DATA_FACTORY = createConditionedDataFactory(
        new SerializableData()
            .addSupplied("phase_down_condition", EntityCondition.DATA_TYPE, () -> new SneakingEntityConditionType().createCondition())
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("render_type", SerializableDataType.enumValue(RenderType.class), RenderType.BLINDNESS)
            .add("view_distance", SerializableDataTypes.POSITIVE_FLOAT, 10.0F)
            .add("blacklist", SerializableDataTypes.BOOLEAN, false),
        (data, condition) -> new PhasingPowerType(
            data.get("phase_down_condition"),
            data.get("block_condition"),
            data.get("render_type"),
            data.get("view_distance"),
            data.get("blacklist"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("phase_down_condition", powerType.phaseDownCondition)
            .set("block_condition", powerType.blockCondition)
            .set("render_type", powerType.getRenderType())
            .set("view_distance", powerType.getViewDistance())
            .set("blacklist", powerType.blacklist)
    );

    private final EntityCondition phaseDownCondition;
    private final Optional<BlockCondition> blockCondition;

    private final RenderType renderType;
    private final float viewDistance;

    private final boolean blacklist;

    public PhasingPowerType(EntityCondition phaseDownCondition, Optional<BlockCondition> blockCondition, RenderType renderType, float viewDistance, boolean blacklist, Optional<EntityCondition> condition) {
        super(condition);
        this.phaseDownCondition = phaseDownCondition;
        this.blockCondition = blockCondition;
        this.blacklist = blacklist;
        this.renderType = renderType;
        this.viewDistance = viewDistance;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PHASING;
    }

    public boolean doesApply(BlockPos pos) {
        return blockCondition
            .map(condition -> blacklist != condition.test(getHolder().getWorld(), pos))
            .orElse(true);
    }

    public boolean shouldPhase(VoxelShape shape, BlockPos pos) {
        LivingEntity holder = getHolder();
        return (holder.getY() < (double) pos.getY() + shape.getMax(Direction.Axis.Y) - (holder.isOnGround() ? 8.05 / 16.0 : 0.0015) || this.shouldPhaseDown())
            && this.doesApply(pos);
    }

    public boolean shouldPhaseDown() {
        return phaseDownCondition.test(getHolder());
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public float getViewDistance() {
        return viewDistance;
    }

    public enum RenderType {
        BLINDNESS, REMOVE_BLOCKS, NONE
    }

    public static boolean shouldPhase(ShapeContext context, VoxelShape shape, BlockPos pos) {
        return context instanceof EntityShapeContext entityContext
            && PowerHolderComponent.hasPowerType(entityContext.getEntity(), PhasingPowerType.class, p -> p.shouldPhase(shape, pos));
    }

}
