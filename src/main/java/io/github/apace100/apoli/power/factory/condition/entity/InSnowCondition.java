package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.WeatherView;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class InSnowCondition {

	public static boolean condition(SerializableData.Instance data, Entity entity) {

		BlockPos downBlockPos = entity.getBlockPos();
		BlockPos upBlockPos = BlockPos.ofFloored(downBlockPos.getX(), entity.getBoundingBox().maxY, downBlockPos.getX());

		return ((WeatherView) entity.world).inSnow(downBlockPos, upBlockPos);

	}

	public static ConditionFactory<Entity> getFactory() {
		return new ConditionFactory<>(
			Apoli.identifier("in_snow"),
			new SerializableData(),
			InSnowCondition::condition
		);
	}

}
