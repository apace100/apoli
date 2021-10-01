package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class TooltipPower extends Power {

    private final Predicate<ItemStack> itemCondition;
    private final List<Text> texts = new LinkedList<>();

    public TooltipPower(PowerType<?> type, LivingEntity entity, Predicate<ItemStack> itemCondition) {
        super(type, entity);
        this.itemCondition = itemCondition;
    }

    public void addText(Text text) {
        texts.add(text);
    }

    public void addToTooltip(List<Text> tooltip) {
        tooltip.addAll(texts);
    }

    public boolean doesApply(ItemStack stack) {
        return itemCondition == null || itemCondition.test(stack);
    }
}
