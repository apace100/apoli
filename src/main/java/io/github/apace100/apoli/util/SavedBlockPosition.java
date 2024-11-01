package io.github.apace100.apoli.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public final class SavedBlockPosition extends CachedBlockPosition {

    private final BlockState blockState;
    private final BlockEntity blockEntity;

    public SavedBlockPosition(WorldView world, BlockPos pos, Function<BlockPos, BlockState> blockStateGetter, Function<BlockPos, BlockEntity> blockEntityGetter) {
        super(world, pos, false);
        this.blockState = blockStateGetter.apply(pos);
        this.blockEntity = blockEntityGetter.apply(pos);
    }

    public SavedBlockPosition(WorldView world, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity) {
        this(world, pos, _pos -> blockState, _pos -> blockEntity);
    }

    public SavedBlockPosition(WorldView world, BlockPos pos) {
        this(world, pos, _pos -> world.isChunkLoaded(_pos) ? world.getBlockState(pos) : null, world::getBlockEntity);
    }

    public static SavedBlockPosition fromLootContext(LootContext context) {

        Vec3d origin = Optional
            .ofNullable(context.get(LootContextParameters.ORIGIN))
            .orElse(Vec3d.ZERO);

        return new SavedBlockPosition(
            context.getWorld(),
            BlockPos.ofFloored(origin),
            context.get(LootContextParameters.BLOCK_STATE),
            context.get(LootContextParameters.BLOCK_ENTITY)
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
