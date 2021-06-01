package io.github.apace100.apoli.power;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.function.Predicate;

public class PreventSleepPower extends Power {

    private final Predicate<CachedBlockPosition> predicate;
    private final String message;
    private final boolean allowSpawnPoint;

    public PreventSleepPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> predicate, String message, boolean allowSpawnPoint) {
        super(type, entity);
        this.predicate = predicate;
        this.message = message;
        this.allowSpawnPoint = allowSpawnPoint;
    }

    public boolean doesPrevent(WorldView world, BlockPos pos) {
        CachedBlockPosition cbp = new CachedBlockPosition(world, pos, true);
        return predicate.test(cbp);
    }

    public String getMessage() {
        return message;
    }

    public boolean doesAllowSpawnPoint() {
        return allowSpawnPoint;
    }
}
