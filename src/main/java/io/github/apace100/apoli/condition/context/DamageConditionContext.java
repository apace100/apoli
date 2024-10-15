package io.github.apace100.apoli.condition.context;

import net.minecraft.entity.damage.DamageSource;

public record DamageConditionContext(DamageSource source, float amount) {

}
