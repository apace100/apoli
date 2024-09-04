package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.factory.DistanceFromCoordinatesConditionRegistry;
import io.github.apace100.apoli.condition.type.block.*;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

public class BlockConditionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static void register() {
        MetaConditionTypes.register(ApoliDataTypes.BLOCK_CONDITION, BlockConditionTypes::register);
        register(OffsetConditionType.getFactory());

        register(HeightConditionType.getFactory());
        DistanceFromCoordinatesConditionRegistry.registerBlockCondition(BlockConditionTypes::register);
        register(BlockConditionType.getFactory());
        register(InTagConditionType.getFactory());
        register(AdjacentConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("replaceable"), cachedBlock -> cachedBlock.getBlockState().isReplaceable()));
        register(createSimpleFactory(Apoli.identifier("attachable"), AttachableConditionType::condition));
        register(FluidConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("movement_blocking"), MovementBlockingConditionType::condition));
        register(createSimpleFactory(Apoli.identifier("light_blocking"), cachedBlock -> cachedBlock.getBlockState().isOpaque()));
        register(createSimpleFactory(Apoli.identifier("water_loggable"), cachedBlock -> cachedBlock.getBlockState().getBlock() instanceof FluidFillable));
        register(createSimpleFactory(Apoli.identifier("exposed_to_sky"), cachedBlock -> cachedBlock.getWorld().isSkyVisible(cachedBlock.getBlockPos())));
        register(LightLevelConditionType.getFactory());
        register(BlockStateConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("block_entity"), cachedBlock -> cachedBlock.getBlockEntity() != null));
        register(NbtConditionType.getFactory());
        register(SlipperinessConditionType.getFactory());
        register(BlastResistanceConditionType.getFactory());
        register(HardnessConditionType.getFactory());
        register(CommandConditionType.getFactory());
    }

    public static ConditionTypeFactory<CachedBlockPosition> createSimpleFactory(Identifier id, Predicate<CachedBlockPosition> condition) {
        return new ConditionTypeFactory<>(id, new SerializableData(), (data, cachedBlock) -> condition.test(cachedBlock));
    }

    public static <F extends ConditionTypeFactory<CachedBlockPosition>> F register(F conditionFactory) {
        return Registry.register(ApoliRegistries.BLOCK_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }

}
