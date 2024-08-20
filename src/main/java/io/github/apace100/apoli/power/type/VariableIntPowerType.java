package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.power.Power;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.util.math.MathHelper;

public class VariableIntPowerType extends PowerType {

    protected final int min, max;
    protected int currentValue;

    public VariableIntPowerType(Power power, LivingEntity entity, int startValue, int min, int max) {
        super(power, entity);
        this.currentValue = startValue;
        this.min = min;
        this.max = max;
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
