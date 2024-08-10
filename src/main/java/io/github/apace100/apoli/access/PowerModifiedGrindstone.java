package io.github.apace100.apoli.access;

import io.github.apace100.apoli.power.type.ModifyGrindstonePowerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PowerModifiedGrindstone {

    List<ModifyGrindstonePowerType> apoli$getAppliedPowers();

    PlayerEntity apoli$getPlayer();

    @Nullable
    BlockPos apoli$getPos();

}
