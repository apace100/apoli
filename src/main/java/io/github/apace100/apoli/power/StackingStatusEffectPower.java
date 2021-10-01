package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

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
    public Tag toTag() {
        return IntTag.of(currentStack);
    }

    @Override
    public void fromTag(Tag tag) {
        currentStack = ((IntTag)tag).getInt();
    }
}
