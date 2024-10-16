package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.inventory.StackReference;
import net.minecraft.world.World;

public class ConsumeItemActionType extends ItemActionType {

    public static final DataObjectFactory<ConsumeItemActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1),
        data -> new ConsumeItemActionType(
            data.get("amount")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("amount", actionType.amount)
    );

    private final int amount;

    public ConsumeItemActionType(int amount) {
        this.amount = amount;
    }

    @Override
    public void execute(World world, StackReference stackReference) {
        stackReference.get().decrement(amount);
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return ItemActionTypes.CONSUME;
    }

}
