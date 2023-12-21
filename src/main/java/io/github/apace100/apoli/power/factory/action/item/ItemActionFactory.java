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

    public static ItemActionFactory createItemStackBased(Identifier identifier, SerializableData data, BiConsumer<SerializableData.Instance, Pair<World, ItemStack>> itemEffect) {

        ItemActionFactory actionFactory = new ItemActionFactory(identifier, data, null);
        actionFactory.legacyEffect = itemEffect;

        return actionFactory;

    }

    public class Instance extends ActionFactory<Pair<World, StackReference>>.Instance {

        protected Instance(SerializableData.Instance data) {
            super(data);
        }

        @Override
        public void accept(Pair<World, StackReference> worldAndStackReference) {

            if (worldAndStackReference.getRight() == StackReference.EMPTY) {
                return;
            }

            ItemStack stack = worldAndStackReference.getRight().get() == ItemStack.EMPTY ? new ItemStack((Void) null) : worldAndStackReference.getRight().get();
            boolean workableEmptyStack = worldAndStackReference.getRight().set(stack);

            if (ItemActionFactory.this.effect != null) {
                ItemActionFactory.this.effect.accept(this.dataInstance, worldAndStackReference);
            }

            if (ItemActionFactory.this.legacyEffect != null) {
                ItemActionFactory.this.legacyEffect.accept(this.dataInstance, new Pair<>(worldAndStackReference.getLeft(), stack));
            }

            if (worldAndStackReference.getRight().get().isEmpty() && (workableEmptyStack || ModifyEnchantmentLevelPower.isWorkableEmptyStack(worldAndStackReference.getRight()))) {
                worldAndStackReference.getRight().set(ItemStack.EMPTY);
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
