package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.function.Predicate;

public class ModifyFluidRenderPowerType extends PowerType {

    private final Predicate<CachedBlockPosition> blockCondition;
    private final Predicate<FluidState> fluidCondition;
    private final FluidState fluidState;

    public ModifyFluidRenderPowerType(Power power, LivingEntity entity, Predicate<CachedBlockPosition> blockCondition, Predicate<FluidState> fluidCondition, FluidState state) {
        super(power, entity);
        this.blockCondition = blockCondition;
        this.fluidCondition = fluidCondition;
        this.fluidState = state;
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

    public boolean doesPrevent(WorldView world, BlockPos pos) {
        return (blockCondition == null || blockCondition.test(new CachedBlockPosition(world, pos, true)))
            && (fluidCondition == null || fluidCondition.test(world.getFluidState(pos)));
    }

    public FluidState getFluidState() {
        return fluidState;
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_fluid_render"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("fluid_condition", ApoliDataTypes.FLUID_CONDITION, null)
                .add("fluid", SerializableDataTypes.FLUID),
            data -> (power, entity) -> new ModifyFluidRenderPowerType(power, entity,
                data.get("block_condition"),
                data.get("fluid_condition"),
                data.get("fluid")
            )
        );
    }

}
