package io.github.apace100.apoli.power;

import io.github.apace100.apoli.ApoliClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.function.Predicate;

public class ModifyBlockRenderPower extends Power {

    private final Predicate<CachedBlockPosition> predicate;
    private final BlockState blockState;

    public ModifyBlockRenderPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> predicate, BlockState state) {
        super(type, entity);
        this.predicate = predicate;
        this.blockState = state;
    }

    public boolean doesPrevent(WorldView world, BlockPos pos) {
        CachedBlockPosition cbp = new CachedBlockPosition(world, pos, true);
        return predicate == null || predicate.test(cbp);
    }

    public BlockState getBlockState() {
        return blockState;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void onAdded() {
        super.onAdded();
        ApoliClient.shouldReloadWorldRenderer = true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void onRemoved() {
        super.onRemoved();
        ApoliClient.shouldReloadWorldRenderer = true;
    }
}
