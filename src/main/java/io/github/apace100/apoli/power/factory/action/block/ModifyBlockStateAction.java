package io.github.apace100.apoli.power.factory.action.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.ResourceOperation;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Collection;
import java.util.Optional;

public class ModifyBlockStateAction {

    public static void action(SerializableData.Instance data, Triple<World, BlockPos, Direction> block) {
        BlockState state = block.getLeft().getBlockState(block.getMiddle());
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
            if(data.getBoolean("cycle")) {
                block.getLeft().setBlockState(block.getMiddle(), state.cycle(property));
            } else {
                Object value = state.get(property);
                if(data.isPresent("enum") && value instanceof Enum) {
                    modifyEnumState(block.getLeft(), block.getMiddle(), state, property, data.getString("enum"));
                } else if(data.isPresent("value") && value instanceof Boolean) {
                    block.getLeft().setBlockState(block.getMiddle(), state.with((Property<Boolean>) property, data.getBoolean("value")));
                } else if(data.isPresent("operation") && data.isPresent("change") && value instanceof Integer) {
                    ResourceOperation op = data.get("operation");
                    int opValue = data.getInt("change");
                    int newValue = (int)value;
                    switch(op) {
                        case ADD -> newValue += opValue;
                        case SET -> newValue = opValue;
                    }
                    Property<Integer> integerProperty = (Property<Integer>) property;
                    if(integerProperty.getValues().contains(newValue)) {
                        block.getLeft().setBlockState(block.getMiddle(), state.with(integerProperty, newValue));
                    }
                }
            }
        }
    }

    private static <T extends Comparable<T>> void modifyEnumState(World world, BlockPos pos, BlockState originalState, Property<T> property, String value) {
        Optional<T> enumValue = property.parse(value);
        enumValue.ifPresent(v -> world.setBlockState(pos, originalState.with(property, v)));
    }

    public static ActionFactory<Triple<World, BlockPos, Direction>> getFactory() {
        return new ActionFactory<>(Apoli.identifier("modify_block_state"),
            new SerializableData()
                .add("property", SerializableDataTypes.STRING)
                .add("operation", ApoliDataTypes.RESOURCE_OPERATION, ResourceOperation.ADD)
                .add("change", SerializableDataTypes.INT, null)
                .add("value", SerializableDataTypes.BOOLEAN, null)
                .add("enum", SerializableDataTypes.STRING, null)
                .add("cycle", SerializableDataTypes.BOOLEAN, false),
            ModifyBlockStateAction::action
        );
    }
}
