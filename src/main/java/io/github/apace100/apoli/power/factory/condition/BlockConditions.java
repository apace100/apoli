package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.block.MaterialCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;

import java.util.Collection;

public class BlockConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        MetaConditions.register(ApoliDataTypes.BLOCK_CONDITION, BlockConditions::register);
        register(new ConditionFactory<>(Apoli.identifier("offset"), new SerializableData()
            .add("condition", ApoliDataTypes.BLOCK_CONDITION)
            .add("x", SerializableDataTypes.INT, 0)
            .add("y", SerializableDataTypes.INT, 0)
            .add("z", SerializableDataTypes.INT, 0),
            (data, block) -> ((ConditionFactory<CachedBlockPosition>.Instance)data.get("condition"))
                .test(new CachedBlockPosition(
                    block.getWorld(),
                    block.getBlockPos().add(
                        data.getInt("x"),
                        data.getInt("y"),
                        data.getInt("z")
                    ), true))));

        register(new ConditionFactory<>(Apoli.identifier("height"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, block) -> ((Comparison)data.get("comparison")).compare(block.getBlockPos().getY(), data.getInt("compare_to"))));
        DistanceFromCoordinatesConditionRegistry.registerBlockCondition(BlockConditions::register);
        register(new ConditionFactory<>(Apoli.identifier("block"), new SerializableData()
            .add("block", SerializableDataTypes.BLOCK),
            (data, block) -> block.getBlockState().isOf((Block)data.get("block"))));
        register(new ConditionFactory<>(Apoli.identifier("in_tag"), new SerializableData()
            .add("tag", SerializableDataTypes.BLOCK_TAG),
            (data, block) -> {
                if(block == null || block.getBlockState() == null) {
                    return false;
                }
                return block.getBlockState().isIn((TagKey<Block>) data.get("tag"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("adjacent"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT)
            .add("adjacent_condition", ApoliDataTypes.BLOCK_CONDITION),
            (data, block) -> {
                ConditionFactory<CachedBlockPosition>.Instance adjacentCondition = (ConditionFactory<CachedBlockPosition>.Instance)data.get("adjacent_condition");
                int adjacent = 0;
                for(Direction d : Direction.values()) {
                    if(adjacentCondition.test(new CachedBlockPosition(block.getWorld(), block.getBlockPos().offset(d), true))) {
                        adjacent++;
                    }
                }
                return ((Comparison)data.get("comparison")).compare(adjacent, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("replacable"), new SerializableData(),
            (data, block) -> block.getBlockState().isReplaceable()));
        register(new ConditionFactory<>(Apoli.identifier("attachable"), new SerializableData(),
            (data, block) -> {
                for(Direction d : Direction.values()) {
                    BlockPos adjacent = block.getBlockPos().offset(d);
                    if(block.getWorld().getBlockState(adjacent).isSideSolidFullSquare(block.getWorld(), block.getBlockPos(), d.getOpposite())) {
                        return true;
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("fluid"), new SerializableData()
            .add("fluid_condition", ApoliDataTypes.FLUID_CONDITION),
            (data, block) -> ((ConditionFactory<FluidState>.Instance)data.get("fluid_condition")).test(block.getWorld().getFluidState(block.getBlockPos()))));
        register(new ConditionFactory<>(Apoli.identifier("movement_blocking"), new SerializableData(),
            (data, block) -> block.getBlockState().blocksMovement() && !block.getBlockState().getCollisionShape(block.getWorld(), block.getBlockPos()).isEmpty()));
        register(new ConditionFactory<>(Apoli.identifier("light_blocking"), new SerializableData(),
            (data, block) -> block.getBlockState().isOpaque()));
        register(new ConditionFactory<>(Apoli.identifier("water_loggable"), new SerializableData(),
            (data, block) -> block.getBlockState().getBlock() instanceof FluidFillable));
        register(new ConditionFactory<>(Apoli.identifier("exposed_to_sky"), new SerializableData(),
            (data, block) -> block.getWorld().isSkyVisible(block.getBlockPos())));
        register(new ConditionFactory<>(Apoli.identifier("light_level"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT)
            .add("light_type", SerializableDataType.enumValue(LightType.class), null),
            (data, block) -> {
                int value;
                if(data.isPresent("light_type")) {
                    LightType lightType = (LightType)data.get("light_type");
                    value = block.getWorld().getLightLevel(lightType, block.getBlockPos());
                } else {
                    value = block.getWorld().getLightLevel(block.getBlockPos());
                }
                return ((Comparison)data.get("comparison")).compare(value, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("block_state"), new SerializableData()
            .add("property", SerializableDataTypes.STRING)
            .add("comparison", ApoliDataTypes.COMPARISON, null)
            .add("compare_to", SerializableDataTypes.INT, null)
            .add("value", SerializableDataTypes.BOOLEAN, null)
            .add("enum", SerializableDataTypes.STRING, null),
            (data, block) -> {
                BlockState state = block.getBlockState();
                Collection<Property<?>> properties = state.getProperties();
                String desiredPropertyName = data.getString("property");
                Property<?> property = null;
                for(Property<?> p : properties) {
                    if(p.getName().equals(desiredPropertyName)) {
                        property = p;
                        break;
                    }
                }
                if(property != null) {
                    Object value = state.get(property);
                    if(data.isPresent("enum") && value instanceof Enum) {
                        return ((Enum)value).name().equalsIgnoreCase(data.getString("enum"));
                    } else if(data.isPresent("value") && value instanceof Boolean) {
                        return (Boolean) value == data.getBoolean("value");
                    } else if(data.isPresent("comparison") && data.isPresent("compare_to") && value instanceof Integer) {
                        return ((Comparison)data.get("comparison")).compare((Integer) value, data.getInt("compare_to"));
                    }
                    return true;
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("block_entity"), new SerializableData(),
            (data, block) -> block.getBlockEntity() != null));
        register(new ConditionFactory<>(Apoli.identifier("nbt"), new SerializableData()
            .add("nbt", SerializableDataTypes.NBT),
            (data, block) -> {
                NbtCompound nbt = new NbtCompound();
                if(block.getBlockEntity() != null) {
                    nbt = block.getBlockEntity().createNbtWithIdentifyingData();
                }
                return NbtHelper.matches((NbtCompound)data.get("nbt"), nbt, true);
            }));
        register(new ConditionFactory<>(Apoli.identifier("slipperiness"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, block) -> {
                BlockState state = block.getBlockState();
                return ((Comparison)data.get("comparison")).compare(state.getBlock().getSlipperiness(), data.getFloat("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("blast_resistance"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, block) -> {
                BlockState state = block.getBlockState();
                return ((Comparison)data.get("comparison")).compare(state.getBlock().getBlastResistance(), data.getFloat("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("hardness"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, block) -> {
                BlockState state = block.getBlockState();
                return ((Comparison)data.get("comparison")).compare(state.getBlock().getHardness(), data.getFloat("compare_to"));
            }));
        register(MaterialCondition.getFactory());
    }

    private static void register(ConditionFactory<CachedBlockPosition> conditionFactory) {
        Registry.register(ApoliRegistries.BLOCK_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
