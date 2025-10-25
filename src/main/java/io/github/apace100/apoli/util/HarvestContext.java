package io.github.apace100.apoli.util;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class HarvestContext {
    private static final ThreadLocal<SavedBlockPosition> BLOCK_POSITION = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> CAN_HARVEST = new ThreadLocal<>();

    public static void setBlockPosition(ServerWorld world, BlockPos pos) {
        BLOCK_POSITION.set(new SavedBlockPosition(world, pos));
    }

    public static SavedBlockPosition getBlockPosition() {
        return BLOCK_POSITION.get();
    }

    public static void clearBlockPosition() {
        BLOCK_POSITION.remove();
    }

    public static void setCanHarvest(Boolean bool) {
        CAN_HARVEST.set(bool);
    }

    public static Boolean getCanHarvest() {
        return CAN_HARVEST.get();
    }

    public static void clearCanHarvest() {
        CAN_HARVEST.remove();
    }
}
