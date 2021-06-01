package io.github.apace100.apoli.power;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class PhasingPower extends Power {

    private final Predicate<CachedBlockPosition> blocks;
    private final boolean isBlacklist;

    private final Predicate<PlayerEntity> phaseDownCondition;

    private final RenderType renderType;
    private final float viewDistance;

    public PhasingPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> blocks, boolean isBlacklist,
                        RenderType renderType, float viewDistance, Predicate<PlayerEntity> phaseDownCondition) {
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

    public boolean shouldPhaseDown(PlayerEntity playerEntity) {
        return phaseDownCondition == null ? playerEntity.isSneaking() : phaseDownCondition.test(playerEntity);
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
