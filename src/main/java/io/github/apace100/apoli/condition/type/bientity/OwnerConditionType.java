package io.github.apace100.apoli.condition.type.bientity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.Tameable;

import java.util.Objects;

public class OwnerConditionType {

	public static boolean condition(Entity actor, Entity target) {
		return (target instanceof Tameable tameable && Objects.equals(actor, tameable.getOwner()))
			|| (target instanceof Ownable ownable && Objects.equals(actor, ownable.getOwner()));
	}

}
