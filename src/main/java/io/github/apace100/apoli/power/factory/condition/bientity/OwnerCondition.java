package io.github.apace100.apoli.power.factory.condition.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.Tameable;
import net.minecraft.util.Pair;

public class OwnerCondition {

	public static boolean condition(SerializableData.Instance data, Pair<Entity, Entity> actorAndTarget) {

		Entity actor = actorAndTarget.getLeft();
		Entity target = actorAndTarget.getRight();

		return (target instanceof Tameable tamable && actor == tamable.getOwner())
			|| (target instanceof Ownable ownable && actor == ownable.getOwner());

	}

	public static ConditionFactory<Pair<Entity, Entity>> getFactory() {
		return new ConditionFactory<>(
			Apoli.identifier("owner"),
			new SerializableData(),
			OwnerCondition::condition
		);
	}

}
