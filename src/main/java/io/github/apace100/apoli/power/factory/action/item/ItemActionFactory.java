package io.github.apace100.apoli.power.factory.action.item;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.function.BiConsumer;

public class ItemActionFactory extends ActionFactory<Pair<World, StackReference>> {

    protected BiConsumer<SerializableData.Instance, Pair<World, ItemStack>> legacyEffect;

    public ItemActionFactory(Identifier identifier, SerializableData data, BiConsumer<SerializableData.Instance, Pair<World, StackReference>> effect) {
        super(identifier, data, effect);
    }

    public static ItemActionFactory createItemStackBased(Identifier identifier, SerializableData data, BiConsumer<SerializableData.Instance, Pair<World, ItemStack>> legacyEffect) {

        ItemActionFactory actionFactory = new ItemActionFactory(identifier, data, null);
        actionFactory.legacyEffect = legacyEffect;

        return actionFactory;

    }

    public class Instance extends ActionFactory<Pair<World, StackReference>>.Instance {

        protected Instance(SerializableData.Instance data) {
            super(data);
        }

        @Override
        public void accept(Pair<World, StackReference> worldAndStackReference) {

            //  Skip empty stack references since those don't really matter
            if (worldAndStackReference.getRight() == StackReference.EMPTY) {
                return;
            }

            World world = worldAndStackReference.getLeft();
            StackReference stackReference = worldAndStackReference.getRight();

            //  Check if the stack of the stack reference is an instance of ItemStack.EMPTY, which
            //  means that it's not a "workable" empty stack
            boolean wasntWorkableEmptyStack = stackReference.get() == ItemStack.EMPTY;

            if (wasntWorkableEmptyStack) {
                //  ModifyEnchantmentLevelPower#getOrCreateWorkableEmptyStack is not used here because
                //  we don't have access to the owner of the stack reference...
                stackReference.set(new ItemStack((Void) null));
            }

            if (ItemActionFactory.this.effect != null) {
                ItemActionFactory.this.effect.accept(this.dataInstance, worldAndStackReference);
            }

            if (ItemActionFactory.this.legacyEffect != null) {
                ItemActionFactory.this.legacyEffect.accept(this.dataInstance, new Pair<>(world, stackReference.get()));
            }

            //  Replace the "workable" empty stack in the stack reference with ItemStack.EMPTY
            if (stackReference.get().isEmpty() && (wasntWorkableEmptyStack || ModifyEnchantmentLevelPower.isWorkableEmptyStack(stackReference))) {
                stackReference.set(ItemStack.EMPTY);
            }

        }

    }

    public ItemActionFactory.Instance read(JsonObject json) {
        return new ItemActionFactory.Instance(data.read(json));
    }

    public ItemActionFactory.Instance read(PacketByteBuf buffer) {
        return new ItemActionFactory.Instance(data.read(buffer));
    }

}
