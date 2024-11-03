package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class GlowingEntityConditionType extends EntityConditionType {

	@Override
	public boolean test(Entity entity) {
		return !entity.getWorld().isClient()
			? entity.isGlowing()
			: MinecraftClient.getInstance().hasOutline(entity);
	}

	@Override
	public @NotNull ConditionConfiguration<?> getConfig() {
		return EntityConditionTypes.GLOWING;
	}

}
