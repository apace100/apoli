package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;

import java.util.List;

public class StackingStatusEffectPower extends StatusEffectPower {

    private final int minStack;
    private final int maxStack;
    private final int durationPerStack;
    private final int tickRate;

    private int currentStack;

    public StackingStatusEffectPower(PowerType<?> type, LivingEntity entity, int minStack, int maxStack, int durationPerStack, int tickRate) {
        super(type, entity);
        this.minStack = minStack;
        this.maxStack = maxStack;
        this.durationPerStack = durationPerStack;
        this.tickRate = tickRate;
        this.setTicking(true);
    }

    public void tick() {
        if(entity.age % tickRate == 0) {
            if(isActive()) {
                currentStack += 1;
                if(currentStack > maxStack) {
                    currentStack = maxStack;
                }
                if(currentStack > 0) {
                    applyEffects();
                }
            } else {
                currentStack -= 1;
                if(currentStack < minStack) {
                    currentStack = minStack;
                }
            }
        }
    }

    @Override
    public void applyEffects() {
        effects.forEach(sei -> {
            int duration = durationPerStack * currentStack;
            if(duration > 0) {
                StatusEffectInstance applySei = new StatusEffectInstance(sei.getEffectType(), duration, sei.getAmplifier(), sei.isAmbient(), sei.shouldShowParticles(), sei.shouldShowIcon());
                entity.addStatusEffect(applySei);
            }
        });
    }

    @Override
    public NbtElement toTag() {
        return NbtInt.of(currentStack);
    }

    @Override
    public void fromTag(NbtElement tag) {
        currentStack = ((NbtInt)tag).intValue();
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("stacking_status_effect"),
            new SerializableData()
                .add("min_stacks", SerializableDataTypes.INT)
                .add("max_stacks", SerializableDataTypes.INT)
                .add("duration_per_stack", SerializableDataTypes.INT)
                .add("tick_rate", SerializableDataTypes.POSITIVE_INT, 10)
                .add("effect", SerializableDataTypes.STATUS_EFFECT_INSTANCE, null)
                .add("effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES, null),
            data -> (powerType, livingEntity) -> {

                StackingStatusEffectPower power = new StackingStatusEffectPower(
                    powerType,
                    livingEntity,
                    data.getInt("min_stacks"),
                    data.getInt("max_stacks"),
                    data.getInt("duration_per_stack"),
                    data.getInt("tick_rate")
                );

                data.<StatusEffectInstance>ifPresent("effect", power::addEffect);
                data.<List<StatusEffectInstance>>ifPresent("effects", effects -> effects.forEach(power::addEffect));

                return power;

            }
        ).allowCondition();
    }

}
