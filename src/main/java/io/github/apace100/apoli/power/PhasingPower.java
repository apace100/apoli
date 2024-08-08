package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import java.util.function.Predicate;

public class PhasingPower extends Power {

    private final Predicate<CachedBlockPosition> blockCondition;
    private final boolean blacklist;

    private final Predicate<Entity> phaseDownCondition;

    private final RenderType renderType;
    private final float viewDistance;

    public PhasingPower(PowerType type, LivingEntity entity, Predicate<CachedBlockPosition> blockCondition, boolean blacklist, RenderType renderType, float viewDistance, Predicate<Entity> phaseDownCondition) {
        super(type, entity);
        this.blockCondition = blockCondition;
        this.blacklist = blacklist;
        this.renderType = renderType;
        this.viewDistance = viewDistance;
        this.phaseDownCondition = phaseDownCondition;
    }

    public boolean doesApply(BlockPos pos) {
        return blockCondition == null
            || blacklist != blockCondition.test(new CachedBlockPosition(entity.getWorld(), pos, true));
    }

    public boolean shouldPhase(VoxelShape shape, BlockPos pos) {
        return (entity.getY() < (double) pos.getY() + shape.getMax(Direction.Axis.Y) - (entity.isOnGround() ? 8.05 / 16.0 : 0.0015)
            || this.shouldPhaseDown())
            && this.doesApply(pos);
    }

    public boolean shouldPhaseDown() {
        return phaseDownCondition != null
            ? phaseDownCondition.test(entity)
            : entity.isSneaking();
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
            && PowerHolderComponent.hasPower(entityContext.getEntity(), PhasingPower.class, p -> p.shouldPhase(shape, pos));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("phasing"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("blacklist", SerializableDataTypes.BOOLEAN, false)
                .add("render_type", SerializableDataType.enumValue(PhasingPower.RenderType.class), PhasingPower.RenderType.BLINDNESS)
                .add("view_distance", SerializableDataTypes.FLOAT, 10F)
                .add("phase_down_condition", ApoliDataTypes.ENTITY_CONDITION, null),
            data -> (type, entity) -> new PhasingPower(
                type,
                entity,
                data.get("block_condition"),
                data.getBoolean("blacklist"),
                data.get("render_type"),
                data.getFloat("view_distance"),
                data.get("phase_down_condition")
            )
        ).allowCondition();
    }
}
