package io.github.apace100.apoli.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;

import java.util.function.Function;

public class SavedBlockPosition extends CachedBlockPosition {

    private final BlockState blockState;
    private final BlockEntity blockEntity;

    public SavedBlockPosition(WorldView world, BlockPos pos) {
        this(world, pos, true);
    }

    public SavedBlockPosition(WorldView world, BlockPos pos, boolean forceload) {
        this(world, pos, world::getBlockState, world::getBlockEntity, forceload);
    }

    public SavedBlockPosition(WorldView world, BlockPos pos, Function<BlockPos, BlockState> blockStateFunction, Function<BlockPos, BlockEntity> blockEntityFunction, boolean forceload) {
        super(world, pos, forceload);
        this.blockState = blockStateFunction.apply(pos);
        this.blockEntity = blockEntityFunction.apply(pos);
    }

    public static SavedBlockPosition fromLootContext(LootContext context) {

        Vec3d origin = context.hasParameter(LootContextParameters.ORIGIN)
            ? context.requireParameter(LootContextParameters.ORIGIN)
            : Vec3d.ZERO;

        return new SavedBlockPosition(
            context.getWorld(),
            BlockPos.ofFloored(origin),
            pos -> context.get(LootContextParameters.BLOCK_STATE),
            pos -> context.get(LootContextParameters.BLOCK_ENTITY),
            false
        );

    }

    @Override
    public BlockState getBlockState() {
        return blockState;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return blockEntity;
    }

}
