package io.github.apace100.apoli.action.context;

import net.minecraft.entity.Entity;

public record BiEntityActionContext(Entity actor, Entity target) {
}
