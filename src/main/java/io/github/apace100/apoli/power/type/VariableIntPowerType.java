package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;

public abstract class VariableIntPowerType extends PowerType {

    private final int min, max, startValue;

    private int currentValue;

    public VariableIntPowerType(int startValue, int min, int max, Optional<EntityCondition> condition) {
        super(condition);
        this.currentValue = startValue;
        this.min = min;
        this.max = max;
        this.startValue = startValue;
    }

    public VariableIntPowerType(int startValue, int min, int max) {
        this(startValue, min, max, Optional.empty());
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getValue() {
        return currentValue;
    }

    public int getStartValue() {
        return startValue;
    }

    public int setValue(int newValue) {
        return currentValue = MathHelper.clamp(newValue, min, max);
    }

    public int increment() {
        return setValue(getValue() + 1);
    }

    public int decrement() {
        return setValue(getValue() - 1);
    }

    @Override
    public NbtElement toTag() {
        return NbtInt.of(currentValue);
    }

    @Override
    public void fromTag(NbtElement tag) {
        currentValue = MathHelper.clamp(((NbtInt) tag).intValue(), min, max);
    }

}
