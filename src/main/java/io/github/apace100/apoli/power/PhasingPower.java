package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class PhasingPower extends Power {

    private final Predicate<CachedBlockPosition> blocks;
    private final boolean isBlacklist;

    private final Predicate<Entity> phaseDownCondition;

    private final RenderType renderType;
    private final float viewDistance;

    public PhasingPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> blocks, boolean isBlacklist,
                        RenderType renderType, float viewDistance, Predicate<Entity> phaseDownCondition) {
        super(type, entity);
        this.blocks = blocks;
        this.isBlacklist = isBlacklist;
        this.renderType = renderType;
        this.viewDistance = viewDistance;
        this.phaseDownCondition = phaseDownCondition;
    }

    public boolean doesApply(BlockPos pos) {
        return isBlacklist != blocks.test(new CachedBlockPosition(entity.getWorld(), pos, true));
    }

    public boolean shouldPhaseDown(Entity entity) {
        return phaseDownCondition == null ? entity.isSneaking() : phaseDownCondition.test(entity);
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

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("phasing"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("blacklist", SerializableDataTypes.BOOLEAN, false)
                .add("render_type", SerializableDataType.enumValue(PhasingPower.RenderType.class), PhasingPower.RenderType.BLINDNESS)
                .add("view_distance", SerializableDataTypes.FLOAT, 10F)
                .add("phase_down_condition", ApoliDataTypes.ENTITY_CONDITION, null),
            data ->
                (type, player) ->
                    new PhasingPower(type, player, data.isPresent("block_condition") ? data.get("block_condition") : cbp -> true,
                        data.getBoolean("blacklist"), data.get("render_type"), data.getFloat("view_distance"),
                        data.get("phase_down_condition")))
            .allowCondition();
    }
}
