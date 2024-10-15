package io.github.apace100.apoli.condition.type.meta;

import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.AbstractCondition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BlockContext;
import io.github.apace100.apoli.condition.context.EntityContext;
import io.github.apace100.apoli.condition.type.AbstractConditionType;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 *	@author Alluysl (refactored by eggohito)
 */
public interface DistanceFromCoordinatesMetaConditionType {

	Reference reference();
	Shape shape();

	Optional<Integer> roundToDigit();
	Vec3d offset();

//	Optional<Boolean> resultOnWrongDimension();
//	boolean checkModifiedSpawns();

	Comparison comparison();
	double compareTo();

	boolean scaleReferenceToDimension();
	boolean scaleDistanceToDimension();

	boolean ignoreX();
	boolean ignoreY();
	boolean ignoreZ();

	default boolean condition(Either<BlockContext, EntityContext> context) {

		World world = context.map(BlockContext::world, EntityContext::world);
		BlockPos pos = context.map(BlockContext::pos, EntityContext::blockPos);

		double coordinateScale = world.getDimension().coordinateScale();

		double x = 0;
		double y = 0;
		double z = 0;

		//	Query the reference's scaled coordinates
		switch (reference()) {
//			case PLAYER_SPAWN, PLAYER_NATURAL_SPAWN -> {
			// 	These references are not yet implemented
//			}
			case WORLD_SPAWN -> {

				//	This, and other of it parts, has been commented since other dimensions (or worlds, in Yarn's terms) can have
				//	its own spawn points (which can be set via `/setworldspawn`)	-eggohito

//				if (resultOnWrongDimension().isPresent() && world.getRegistryKey != World.OVERWORLD) {
//					return resultOnWrongDimension().get();
//				}

				BlockPos spawnPos = world.getSpawnPos();

				x = spawnPos.getX();
				y = spawnPos.getY();
				z = spawnPos.getZ();

			}
			case WORLD_ORIGIN -> {
				//	The origin of a world is at 0, 0, 0, so we don't need to do anything at this point
			}
		}

		x += offset().getX();
		y += offset().getY();
		z += offset().getZ();

		if (scaleReferenceToDimension() && (x != 0 || z != 0)) {

			//	Upon further investigation, a dimension cannot have a coordinate scale of absolute 0 as its value is bound from
			//	1.0E-5F (0.00001) to 3.0E7 (30000000), meaning that this section may be unnecessary?	-eggohito

			//	Pocket dimensions?
//			if (coordinateScale == 0) {
			//	A coordinate scale of 0 means that it takes 0 blocks to travel from the overworld to travel 1 block in the dimension,
			//	so the dimension is folded on 0, 0, so unless the overworld reference is at 0, 0, it gets scaled to infinity
//				return outOfBounds(comparison);
//			}

			x /= coordinateScale;
			z /= coordinateScale;

		}

		double xDistance = ignoreX() ? 0 : Math.abs(pos.getX() - x);
		double yDistance = ignoreY() ? 0 : Math.abs(pos.getY() - y);
		double zDistance = ignoreZ() ? 0 : Math.abs(pos.getZ() - z);

		if (scaleDistanceToDimension()) {
			xDistance *= coordinateScale;
			zDistance *= coordinateScale;
		}

		double distance = Shape.getDistance(shape(), xDistance, yDistance, zDistance);
		double scaledDistance = roundToDigit()
			.map(scale -> new BigDecimal(distance).setScale(scale, RoundingMode.HALF_UP).doubleValue())
			.orElse(distance);

		return comparison().compare(scaledDistance, compareTo());

	}

	static <T, C extends AbstractCondition<T, CT>, CT extends AbstractConditionType<T, C>, M extends AbstractConditionType<T, C> & DistanceFromCoordinatesMetaConditionType> ConditionConfiguration<M> createConfiguration(Constructor<M> constructor) {
		return ConditionConfiguration.of(
			Apoli.identifier("distance_from_coordinates"),
			new SerializableData()
				.add("reference", SerializableDataType.enumValue(Reference.class), Reference.WORLD_ORIGIN)	//	The reference point for comparison
				.add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)	//	The shape used for comparing the distance
				.add("round_to_digit", SerializableDataTypes.INT.optional(), Optional.empty())	//	Rounds the calculated distance to this amount of digits (e.g: 0 for unitary values, 1 for decimals, -1 for multiples of ten)
				.add("offset", SerializableDataTypes.VECTOR, Vec3d.ZERO)	//	The offset for the reference point
				.add("comparison", ApoliDataTypes.COMPARISON)
				.add("compare_to", SerializableDataTypes.DOUBLE)
//				.add("result_on_wrong_dimension", SerializableDataTypes.BOOLEAN.optional(), Optional.empty())	//	The value to be used as the result if the dimension is not the same as the reference's
//				.add("check_modified_spawn", SerializableDataTypes.BOOLEAN, true)	//	Determines whether to check for modified spawns
				.add("scale_reference_to_dimension", SerializableDataTypes.BOOLEAN, true)	//	Determines whether to scale the reference's coordinates according to the dimension it's in and the player is in
				.add("scale_distance_to_dimension", SerializableDataTypes.BOOLEAN, false)	//	Determines whether to scale the calculated distance to the current dimension
				.add("ignore_x", SerializableDataTypes.BOOLEAN, false)
				.add("ignore_y", SerializableDataTypes.BOOLEAN, false)
				.add("ignore_z", SerializableDataTypes.BOOLEAN, false),
			data -> constructor.create(
				data.get("reference"),
				data.get("shape"),
				data.get("round_to_digit"),
				data.get("offset"),
				data.get("comparison"),
				data.get("compare_to"),
				data.get("scale_reference_to_dimension"),
				data.get("scale_distance_to_dimension"),
				data.get("ignore_x"),
				data.get("ignore_y"),
				data.get("ignore_z")
			),
			(m, serializableData) -> serializableData.instance()
				.set("reference", m.reference())
				.set("shape", m.shape())
				.set("round_to_digit", m.roundToDigit())
				.set("offset", m.offset())
				.set("comparison", m.comparison())
				.set("compare_to", m.compareTo())
				.set("scale_reference_to_dimension", m.scaleReferenceToDimension())
				.set("scale_distance_to_dimension", m.scaleDistanceToDimension())
				.set("ignore_x", m.ignoreX())
				.set("ignore_y", m.ignoreY())
				.set("ignore_z", m.ignoreZ())
		);
	}

	interface Constructor<M extends AbstractConditionType<?, ?> & DistanceFromCoordinatesMetaConditionType> {
		M create(Reference reference, Shape shape, Optional<Integer> roundToDigit, Vec3d offset, Comparison comparison, double compareTo, boolean scaleReferenceToDimension, boolean scaleDistanceToDimension, boolean ignoreX, boolean ignoreY, boolean ignoreZ);
	}

	enum Reference {
//		PLAYER_SPAWN,
//		PLAYER_NATURAL_SPAWN,
		WORLD_SPAWN,
		WORLD_ORIGIN
	}

}
