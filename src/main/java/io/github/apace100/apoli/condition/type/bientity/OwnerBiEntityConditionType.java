package io.github.apace100.apoli.condition.type.bientity;

import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.Tameable;

import java.util.Objects;

public class OwnerBiEntityConditionType extends BiEntityConditionType {

	@Override
	public ConditionConfiguration<?> configuration() {
		return BiEntityConditionTypes.OWNER;
	}

	@Override
	public boolean test(Entity actor, Entity target) {
		return condition(actor, target);
	}

	public static boolean condition(Entity actor, Entity target) {
		return (target instanceof Tameable tameable && Objects.equals(actor, tameable.getOwner()))
			|| (target instanceof Ownable ownable && Objects.equals(actor, ownable.getOwner()));
	}

}
