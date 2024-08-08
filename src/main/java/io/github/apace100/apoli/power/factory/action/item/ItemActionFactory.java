package io.github.apace100.apoli.power.factory.action.item;

import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class ItemActionFactory extends ActionFactory<Pair<World, StackReference>> {

    public ItemActionFactory(Identifier identifier, SerializableData data, @NotNull BiConsumer<SerializableData.Instance, Pair<World, StackReference>> effect) {
        super(identifier, data, effect);
    }

    public static ItemActionFactory createItemStackBased(Identifier identifier, SerializableData data, @NotNull BiConsumer<SerializableData.Instance, Pair<World, ItemStack>> legacyEffect) {
        return new ItemActionFactory(identifier, data, (data1, worldAndStackRef) -> legacyEffect.accept(data1, new Pair<>(worldAndStackRef.getLeft(), worldAndStackRef.getRight().get())));
    }

    @Override
    public Instance receive(RegistryByteBuf buffer) {
        return new Instance(this.getSerializableData().receive(buffer));
    }

    @Override
    public Instance fromData(SerializableData.Instance data) {
        return new Instance(data);
    }

    public class Instance extends ActionFactory<Pair<World, StackReference>>.Instance {

        protected Instance(SerializableData.Instance data) {
            super(data);
        }

        @Override
        public void accept(Pair<World, StackReference> worldAndStackReference) {

            //  Skip empty stack references since they're practically immutable
            StackReference stackReference = worldAndStackReference.getRight();
            if (stackReference == StackReference.EMPTY) {
                return;
            }

            //  Replace the stack of the stack reference with a "workable" empty stack if the said stack is NOT
            //  already "workable", and if the said stack is an instance of ItemStack#EMPTY
            if (stackReference.get() == ItemStack.EMPTY) {
                stackReference.set(new ItemStack((Void) null));
            }

            //  Execute the specified effect of the item action
            this.effect.accept(worldAndStackReference);

            //  Replace the stack of the stack reference with ItemStack#EMPTY if the said stack is NOT
            //  "workable", and if the said stack is empty
            if (!ModifyEnchantmentLevelPowerType.isWorkableEmptyStack(stackReference) && stackReference.get().isEmpty()) {
                stackReference.set(ItemStack.EMPTY);
            }

        }

    }

}
