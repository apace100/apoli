package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.ResourceOperation;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ModifyBlockStateActionType {

    public static void action(World world, BlockPos pos, String propertyName, ResourceOperation operation, @Nullable Integer change, @Nullable Boolean value, @Nullable String enumName, boolean cycle) {

        BlockState blockState = world.getBlockState(pos);
        Property<?> property = blockState.getProperties()
            .stream()
            .filter(prop -> prop.getName().equals(propertyName))
            .findFirst()
            .orElse(null);

        if (property == null) {
            return;
        }

        if (cycle) {
            world.setBlockState(pos, blockState.cycle(property));
            return;
        }

        switch (property) {
            case EnumProperty<?> enumProp when enumName != null && !enumName.isEmpty() ->
                setEnumProperty(enumProp, enumName, world, pos, blockState);
            case BooleanProperty boolProp when value != null ->
                world.setBlockState(pos, blockState.with(boolProp, value));
            case IntProperty intProp when change != null -> {

                int newValue = switch (operation) {
                    case ADD ->
                        Optional.ofNullable(blockState.get(intProp)).orElse(0) + change;
                    case SET ->
                        change;
                };

                if (intProp.getValues().contains(newValue)) {
                    world.setBlockState(pos, blockState.with(intProp, newValue));
                }

            }
            default -> {

            }
        }

    }

    private static <T extends Enum<T> & StringIdentifiable> void setEnumProperty(EnumProperty<T> property, String name, World world, BlockPos pos, BlockState originalState) {
        property.parse(name).ifPresentOrElse(
            propValue -> world.setBlockState(pos, originalState.with(property, propValue)),
            () -> Apoli.LOGGER.warn("Couldn't set enum property \"{}\" of block at {} to \"{}\"! Expected value to be any of {}", property.getName(), pos.toShortString(), name, String.join(", ", property.getValues().stream().map(StringIdentifiable::asString).toList()))
        );
    }

    public static ActionTypeFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("modify_block_state"),
            new SerializableData()
                .add("property", SerializableDataTypes.STRING)
                .add("operation", ApoliDataTypes.RESOURCE_OPERATION, ResourceOperation.ADD)
                .add("change", SerializableDataTypes.INT, null)
                .add("value", SerializableDataTypes.BOOLEAN, null)
                .add("enum", SerializableDataTypes.STRING, null)
                .add("cycle", SerializableDataTypes.BOOLEAN, false),
            (data, block) -> action(block.getLeft(), block.getMiddle(),
                data.get("property"),
                data.get("operation"),
                data.get("change"),
                data.get("value"),
                data.get("enum"),
                data.get("cycle")
            )
        );
    }
}
