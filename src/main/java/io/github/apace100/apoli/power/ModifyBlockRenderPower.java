package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
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

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_block_render"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("block", SerializableDataTypes.BLOCK_STATE),
            data ->
                (type, player) -> new ModifyBlockRenderPower(type, player,
                        data.get("block_condition"),
                        data.get("block")));
    }
}
