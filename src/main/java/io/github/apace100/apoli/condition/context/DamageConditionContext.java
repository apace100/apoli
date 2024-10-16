package io.github.apace100.apoli.condition.context;

import io.github.apace100.apoli.util.context.TypeConditionContext;
import net.minecraft.entity.damage.DamageSource;

public record DamageConditionContext(DamageSource source, float amount) implements TypeConditionContext {

}
