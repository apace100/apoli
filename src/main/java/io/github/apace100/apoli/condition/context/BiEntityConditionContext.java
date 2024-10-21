package io.github.apace100.apoli.condition.context;

import io.github.apace100.apoli.util.context.TypeConditionContext;
import net.minecraft.entity.Entity;

public record BiEntityConditionContext(Entity actor, Entity target) implements TypeConditionContext {

}
