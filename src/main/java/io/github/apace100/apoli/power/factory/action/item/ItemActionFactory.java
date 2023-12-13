package io.github.apace100.apoli.power.factory.action.item;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
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
    protected BiConsumer<SerializableData.Instance, Pair<World, ItemStack>> itemEffect;

    public ItemActionFactory(Identifier identifier, SerializableData data, BiConsumer<SerializableData.Instance, Pair<World, StackReference>> effect) {
        super(identifier, data, effect);
    }

    public static ItemActionFactory createItemStackBased(Identifier identifier, SerializableData data, BiConsumer<SerializableData.Instance, Pair<World, ItemStack>> itemEffect) {
        ItemActionFactory actionFactory = new ItemActionFactory(identifier, data, null);
        actionFactory.itemEffect = itemEffect;
        return actionFactory;
    }

    public class Instance extends ActionFactory<Pair<World, StackReference>>.Instance {
        protected Instance(SerializableData.Instance data) {
            super(data);
        }

        @Override
        public void accept(Pair<World, StackReference> worldItemStackPair) {
            ItemStack stack = worldItemStackPair.getRight().get() == ItemStack.EMPTY ? new ItemStack((Void)null) : worldItemStackPair.getRight().get();
            worldItemStackPair.getRight().set(stack);
            if (ItemActionFactory.this.effect != null) {
                ItemActionFactory.this.effect.accept(this.dataInstance, worldItemStackPair);
            }
            if (ItemActionFactory.this.itemEffect != null) {
                ItemActionFactory.this.itemEffect.accept(this.dataInstance, new Pair<>(worldItemStackPair.getLeft(), stack));
            }
            if (worldItemStackPair.getRight().get().isEmpty() && (((EntityLinkedItemStack)worldItemStackPair.getRight().get())).apoli$getEntity() != null && ModifyEnchantmentLevelPower.isWorkableEmptyStack((((EntityLinkedItemStack)worldItemStackPair.getRight().get())).apoli$getEntity(), worldItemStackPair.getRight())) {
                worldItemStackPair.getRight().set(ItemStack.EMPTY);
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
