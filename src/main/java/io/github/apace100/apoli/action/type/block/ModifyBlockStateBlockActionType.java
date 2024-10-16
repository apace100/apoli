package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.ResourceOperation;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

public class ModifyBlockStateBlockActionType extends BlockActionType {

    public static final DataObjectFactory<ModifyBlockStateBlockActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("property", SerializableDataTypes.STRING)
            .add("operation", ApoliDataTypes.RESOURCE_OPERATION, ResourceOperation.ADD)
            .add("change", SerializableDataTypes.INT.optional(), Optional.empty())
            .add("value", SerializableDataTypes.BOOLEAN.optional(), Optional.empty())
            .add("enum", SerializableDataTypes.STRING.optional(), Optional.empty())
            .add("cycle", SerializableDataTypes.BOOLEAN, false),
        data -> new ModifyBlockStateBlockActionType(
            data.get("property"),
            data.get("operation"),
            data.get("change"),
            data.get("value"),
            data.get("enum"),
            data.get("cycle")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("property", actionType.property)
            .set("operation", actionType.operation)
            .set("change", actionType.change)
            .set("value", actionType.boolValue)
            .set("enum", actionType.enumValue)
            .set("cycle", actionType.cycle)
    );

    private final String property;

    private final ResourceOperation operation;
    private final Optional<Integer> change;

    private final Optional<Boolean> boolValue;
    private final Optional<String> enumValue;

    private final boolean cycle;

    public ModifyBlockStateBlockActionType(String property, ResourceOperation operation, Optional<Integer> change, Optional<Boolean> boolValue, Optional<String> enumValue, boolean cycle) {
        this.property = property;
        this.operation = operation;
        this.change = change;
        this.boolValue = boolValue;
        this.enumValue = enumValue;
        this.cycle = cycle;
    }

    @Override
	protected void execute(World world, BlockPos pos, Optional<Direction> direction) {

        BlockState blockState = world.getBlockState(pos);
        Property<?> blockProperty = blockState.getProperties()
            .stream()
            .filter(prop -> prop.getName().equals(property))
            .findFirst()
            .orElse(null);

        if (blockProperty == null) {
            return;
        }

        if (cycle) {
            world.setBlockState(pos, blockState.cycle(blockProperty));
            return;
        }

        switch (blockProperty) {
            case EnumProperty<?> enumProp when enumValue.isPresent() && !enumValue.get().isEmpty() ->
                setEnumProperty(enumProp, enumValue.get(), world, pos, blockState);
            case BooleanProperty boolProp when boolValue.isPresent() ->
                world.setBlockState(pos, blockState.with(boolProp, boolValue.get()));
            case IntProperty intProp when change.isPresent() -> {

                int newValue = switch (operation) {
                    case ADD ->
                        Optional.ofNullable(blockState.get(intProp)).orElse(0) + change.get();
                    case SET ->
                        change.get();
                };

                if (intProp.getValues().contains(newValue)) {
                    world.setBlockState(pos, blockState.with(intProp, newValue));
                }

            }
            default -> {

            }
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BlockActionTypes.MODIFY_BLOCK_STATE;
    }

    private static <T extends Enum<T> & StringIdentifiable> void setEnumProperty(EnumProperty<T> property, String name, World world, BlockPos pos, BlockState originalState) {
        property.parse(name).ifPresentOrElse(
            propValue -> world.setBlockState(pos, originalState.with(property, propValue)),
            () -> Apoli.LOGGER.warn("Couldn't set enum property \"{}\" of block at {} to \"{}\"! Expected value to be any of {}", property.getName(), pos.toShortString(), name, String.join(", ", property.getValues().stream().map(StringIdentifiable::asString).toList()))
        );
    }

}
