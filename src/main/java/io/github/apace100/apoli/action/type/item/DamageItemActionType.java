package io.github.apace100.apoli.action.type.item;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class DamageItemActionType extends ItemActionType {

    public static final TypedDataObjectFactory<DamageItemActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1)
            .add("ignore_unbreaking", SerializableDataTypes.BOOLEAN, false),
        data -> new DamageItemActionType(
            data.get("amount"),
            data.get("ignore_unbreaking")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("amount", actionType.amount)
            .set("ignore_unbreaking", actionType.ignoreUnbreaking)
    );

    private final int amount;
    private final boolean ignoreUnbreaking;

    public DamageItemActionType(int amount, boolean ignoreUnbreaking) {
        this.amount = amount;
        this.ignoreUnbreaking = ignoreUnbreaking;
    }

    @Override
	protected void execute(World world, StackReference stackReference) {

        ItemStack stack = stackReference.get();
        if (world instanceof ServerWorld serverWorld) {

            if (ignoreUnbreaking) {

                if (amount >= stack.getMaxDamage()) {
                    stack.decrement(1);
                }

                else {
                    stack.setDamage(stack.getDamage() + amount);
                }

            }

            else {
                stack.damage(amount, serverWorld, null, item -> {});
            }

        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return ItemActionTypes.DAMAGE;
    }

}
