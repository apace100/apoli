package io.github.apace100.apoli.action.context;

import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.util.context.ActionContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public record EntityActionContext(Entity entity, Vec3d offset) implements ActionContext<EntityConditionContext> {

	public EntityActionContext(Entity entity) {
		this(entity, Vec3d.ZERO);
	}

	@Override
	public EntityConditionContext forCondition() {
		return new EntityConditionContext(entity());
	}

	public World world() {
		return entity().getWorld();
	}

}
