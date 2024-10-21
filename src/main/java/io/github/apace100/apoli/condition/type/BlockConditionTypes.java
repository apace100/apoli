package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.block.*;
import io.github.apace100.apoli.condition.type.block.meta.*;
import io.github.apace100.apoli.condition.type.meta.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.registry.Registry;

public class BlockConditionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();
    public static final SerializableDataType<ConditionConfiguration<io.github.apace100.apoli.condition.type.BlockConditionType>> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.BLOCK_CONDITION_TYPE, Apoli.MODID, ALIASES, (configurations, id) -> "Block condition type \"" + id + "\" is undefined!");

    public static final ConditionConfiguration<AllOfBlockConditionType> ALL_OF = register(AllOfMetaConditionType.createConfiguration(BlockCondition.DATA_TYPE, AllOfBlockConditionType::new));
    public static final ConditionConfiguration<AnyOfBlockConditionType> ANY_OF = register(AnyOfMetaConditionType.createConfiguration(BlockCondition.DATA_TYPE, AnyOfBlockConditionType::new));
    public static final ConditionConfiguration<ConstantBlockConditionType> CONSTANT = register(ConstantMetaConditionType.createConfiguration(ConstantBlockConditionType::new));
    public static final ConditionConfiguration<RandomChanceBlockConditionType> RANDOM_CHANCE = register(RandomChanceMetaConditionType.createConfiguration(RandomChanceBlockConditionType::new));

    public static final ConditionConfiguration<DistanceFromCoordinatesBlockConditionType> DISTANCE_FROM_COORDINATES = register(DistanceFromCoordinatesMetaConditionType.createConfiguration(DistanceFromCoordinatesBlockConditionType::new));
    public static final ConditionConfiguration<OffsetBlockConditionType> OFFSET = register(ConditionConfiguration.of(Apoli.identifier("offset"), OffsetBlockConditionType.DATA_FACTORY));

    public static final ConditionConfiguration<AdjacentBlockConditionType> ADJACENT = register(ConditionConfiguration.of(Apoli.identifier("adjacent"), AdjacentBlockConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<AttachableBlockConditionType> ATTACHABLE = register(ConditionConfiguration.simple(Apoli.identifier("attachable"), AttachableBlockConditionType::new));
    public static final ConditionConfiguration<BlastResistanceBlockConditionType> BLAST_RESISTANCE = register(ConditionConfiguration.of(Apoli.identifier("blast_resistance"), BlastResistanceBlockConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<BlockBlockConditionType> BLOCK = register(ConditionConfiguration.of(Apoli.identifier("block"), BlockBlockConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<BlockEntityBlockConditionType> BLOCK_ENTITY = register(ConditionConfiguration.simple(Apoli.identifier("block_entity"), BlockEntityBlockConditionType::new));
    public static final ConditionConfiguration<BlockStateBlockConditionType> BLOCK_STATE = register(ConditionConfiguration.of(Apoli.identifier("block_state"), BlockStateBlockConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<CommandBlockConditionType> COMMAND = register(ConditionConfiguration.of(Apoli.identifier("command"), CommandBlockConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<ExposedToSkyBlockConditionType> EXPOSED_TO_SKY = register(ConditionConfiguration.simple(Apoli.identifier("exposed_to_sky"), ExposedToSkyBlockConditionType::new));
    public static final ConditionConfiguration<FluidBlockConditionType> FLUID = register(ConditionConfiguration.of(Apoli.identifier("fluid"), FluidBlockConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<HardnessBlockConditionType> HARDNESS = register(ConditionConfiguration.of(Apoli.identifier("hardness"), HardnessBlockConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<HeightBlockConditionType> HEIGHT = register(ConditionConfiguration.of(Apoli.identifier("height"), HeightBlockConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<InTagBlockConditionType> IN_TAG = register(ConditionConfiguration.of(Apoli.identifier("in_tag"), InTagBlockConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<LightBlockingBlockConditionType> LIGHT_BLOCKING = register(ConditionConfiguration.simple(Apoli.identifier("light_blocking"), LightBlockingBlockConditionType::new));
    public static final ConditionConfiguration<LightLevelBlockConditionType> LIGHT_LEVEL = register(ConditionConfiguration.of(Apoli.identifier("light_level"), LightLevelBlockConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<MovementBlockingBlockConditionType> MOVEMENT_BLOCKING = register(ConditionConfiguration.simple(Apoli.identifier("movement_blocking"), MovementBlockingBlockConditionType::new));
    public static final ConditionConfiguration<NbtBlockConditionType> NBT = register(ConditionConfiguration.of(Apoli.identifier("nbt"), NbtBlockConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<ReplaceableBlockConditionType> REPLACEABLE = register(ConditionConfiguration.simple(Apoli.identifier("replaceable"), ReplaceableBlockConditionType::new));
    public static final ConditionConfiguration<SlipperinessBlockConditionType> SLIPPERINESS = register(ConditionConfiguration.of(Apoli.identifier("slipperiness"), SlipperinessBlockConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<WaterLoggableBlockConditionType> WATER_LOGGABLE = register(ConditionConfiguration.simple(Apoli.identifier("water_loggable"), WaterLoggableBlockConditionType::new));

	public static void register() {

    }

    @SuppressWarnings("unchecked")
	public static <T extends io.github.apace100.apoli.condition.type.BlockConditionType> ConditionConfiguration<T> register(ConditionConfiguration<T> configuration) {

        ConditionConfiguration<io.github.apace100.apoli.condition.type.BlockConditionType> casted = (ConditionConfiguration<io.github.apace100.apoli.condition.type.BlockConditionType>) configuration;
        Registry.register(ApoliRegistries.BLOCK_CONDITION_TYPE, casted.id(), casted);

        return configuration;

    }

}
