package io.github.apace100.apoli.action.context;

import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.util.context.TypeActionContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public record EntityActionContext(Entity entity) implements TypeActionContext<EntityConditionContext> {

	@Override
	public EntityConditionContext conditionContext() {
		return new EntityConditionContext(entity());
	}

	public World world() {
		return entity().getWorld();
	}

	public Vec3d pos() {
		return entity().getPos();
	}

}
