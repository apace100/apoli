package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.inventory.StackReference;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class MergeCustomDataItemActionType extends ItemActionType {

    public static final DataObjectFactory<MergeCustomDataItemActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("nbt", SerializableDataTypes.NBT_COMPOUND),
        data -> new MergeCustomDataItemActionType(
            data.get("nbt")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("nbt", actionType.nbt)
    );

    private final NbtCompound nbt;

    public MergeCustomDataItemActionType(NbtCompound nbt) {
        this.nbt = nbt;
    }

    @Override
	protected void execute(World world, StackReference stackReference) {
        NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stackReference.get(), oldNbt -> oldNbt.copyFrom(nbt));
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return ItemActionTypes.MERGE_CUSTOM_DATA;
    }

}
