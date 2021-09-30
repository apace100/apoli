package io.github.apace100.apoli.power;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
        return isBlacklist != blocks.test(new CachedBlockPosition(entity.world, pos, true));
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
        BLINDNESS, REMOVE_BLOCKS
    }
}
