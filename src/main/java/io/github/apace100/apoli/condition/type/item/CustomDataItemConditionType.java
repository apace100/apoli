package io.github.apace100.apoli.condition.type.item;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ItemConditionType;
import io.github.apace100.apoli.condition.type.ItemConditionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class CustomDataItemConditionType extends ItemConditionType {

    public static final TypedDataObjectFactory<CustomDataItemConditionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("nbt", SerializableDataTypes.NBT_COMPOUND),
        data -> new CustomDataItemConditionType(
            data.get("nbt")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("nbt", conditionType.nbt)
    );

    private final NbtCompound nbt;

    public CustomDataItemConditionType(NbtCompound nbt) {
        this.nbt = nbt;
    }

    @Override
    public boolean test(World world, ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).matches(nbt);
    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return ItemConditionTypes.CUSTOM_DATA;
    }

}
