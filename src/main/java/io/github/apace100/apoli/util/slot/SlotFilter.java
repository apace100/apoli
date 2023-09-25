package io.github.apace100.apoli.util.slot;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public record SlotFilter(@Nullable Integer slot, @Nullable Predicate<ItemStack> stackPredicate) {

    public boolean testStack(ItemStack stack) {
        return stackPredicate == null || stackPredicate.test(stack);
    }

    public boolean testSlot(int slot) {
        return this.slot == null || this.slot == slot;
    }

    public boolean test(int slot, ItemStack stack) {
        return testSlot(slot)
            && testStack(stack);
    }

}
