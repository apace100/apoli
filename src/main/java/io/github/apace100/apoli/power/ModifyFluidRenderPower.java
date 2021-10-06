package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.function.Predicate;

public class ModifyFluidRenderPower extends Power {

    private final Predicate<CachedBlockPosition> blockCondition;
    private final Predicate<FluidState> fluidCondition;
    private final FluidState fluidState;

    public ModifyFluidRenderPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> blockCondition, Predicate<FluidState> fluidCondition, FluidState state) {
        super(type, entity);
        this.blockCondition = blockCondition;
        this.fluidCondition = fluidCondition;
        this.fluidState = state;
    }

    public boolean doesPrevent(WorldView world, BlockPos pos) {
        CachedBlockPosition cbp = new CachedBlockPosition(world, pos, true);
        if(blockCondition == null || blockCondition.test(cbp)) {
            return fluidCondition == null || fluidCondition.test(world.getFluidState(pos));
        }
        return false;
    }

    public FluidState getFluidState() {
        return fluidState;
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
        return new PowerFactory<>(Apoli.identifier("modify_fluid_render"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("fluid_condition", ApoliDataTypes.FLUID_CONDITION, null)
                .add("fluid", SerializableDataTypes.FLUID),
            data ->
                (type, player) -> new ModifyFluidRenderPower(type, player,
                    (ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition"),
                    (ConditionFactory<FluidState>.Instance)data.get("fluid_condition"),
                    ((Fluid)data.get("fluid")).getDefaultState()));
    }
}
