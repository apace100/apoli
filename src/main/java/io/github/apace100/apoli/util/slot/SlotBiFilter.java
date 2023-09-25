package io.github.apace100.apoli.util.slot;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

public class SlotBiFilter extends Pair<SlotFilter, SlotFilter> {

    public SlotBiFilter(SlotFilter left, SlotFilter right) {
        super(left, right);
    }

    @Override
    @Nullable
    public SlotFilter getLeft() {
        return super.getLeft();
    }

    @Override
    @Nullable
    public SlotFilter getRight() {
        return super.getRight();
    }

    public boolean testStack(SlotFilter slotFilter, ItemStack stack) {
        return slotFilter == null || slotFilter.testStack(stack);
    }

    public boolean testSlot(SlotFilter slotFilter, int slot) {
        return slotFilter == null || slotFilter.testSlot(slot);
    }

}
