package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ModifyBlockRenderPowerType extends PowerType {

    public static final TypedDataObjectFactory<ModifyBlockRenderPowerType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("block", SerializableDataTypes.BLOCK_STATE),
        data -> new ModifyBlockRenderPowerType(
            data.get("block_condition"),
            data.get("block")
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("block_condition", powerType.blockCondition)
            .set("block", powerType.blockState)
    );

    private final Optional<BlockCondition> blockCondition;
    private final BlockState blockState;

    public ModifyBlockRenderPowerType(Optional<BlockCondition> blockCondition, BlockState state) {
        this.blockCondition = blockCondition;
        this.blockState = state;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_BLOCK_RENDER;
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

    public boolean doesPrevent(World world, BlockPos pos) {
        return blockCondition
            .map(condition -> condition.test(world, pos))
            .orElse(true);
    }

    public BlockState getBlockState() {
        return blockState;
    }

}
