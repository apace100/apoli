package io.github.apace100.apoli.action.context;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public record BlockActionContext(World world, BlockPos pos, Optional<Direction> direction) {

}
