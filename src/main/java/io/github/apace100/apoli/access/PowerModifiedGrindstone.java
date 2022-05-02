package io.github.apace100.apoli.access;

import io.github.apace100.apoli.power.ModifyGrindstonePower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;

public interface PowerModifiedGrindstone {

    List<ModifyGrindstonePower> getAppliedPowers();

    PlayerEntity getPlayer();

    Optional<BlockPos> getPos();
}
