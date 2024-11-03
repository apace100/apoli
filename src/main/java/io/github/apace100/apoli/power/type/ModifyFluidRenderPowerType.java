package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.FluidCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ModifyFluidRenderPowerType extends PowerType {

    public static final TypedDataObjectFactory<ModifyFluidRenderPowerType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("fluid_condition", FluidCondition.DATA_TYPE.optional(), Optional.empty())
            .add("fluid", SerializableDataTypes.FLUID),
        data -> new ModifyFluidRenderPowerType(
            data.get("block_condition"),
            data.get("fluid_condition"),
            data.get("fluid")
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("block_condition", powerType.blockCondition)
            .set("fluid_condition", powerType.fluidCondition)
            .set("fluid", powerType.fluid)
    );

    protected final Optional<BlockCondition> blockCondition;
    protected final Optional<FluidCondition> fluidCondition;

    protected final Fluid fluid;

    public ModifyFluidRenderPowerType(Optional<BlockCondition> blockCondition, Optional<FluidCondition> fluidCondition, Fluid fluid) {
        this.blockCondition = blockCondition;
        this.fluidCondition = fluidCondition;
        this.fluid = fluid;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_FLUID_RENDER;
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
        return fluidCondition.map(condition -> condition.test(world.getFluidState(pos))).orElse(true)
            && blockCondition.map(condition -> condition.test(world, pos)).orElse(true);
    }

    public FluidState getFluidState() {
        return fluid.getDefaultState();
    }

}
