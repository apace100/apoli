package io.github.apace100.apoli.condition.type.block;

import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.context.BlockConditionContext;
import io.github.apace100.apoli.condition.type.BlockConditionType;
import io.github.apace100.apoli.condition.type.BlockConditionTypes;
import io.github.apace100.apoli.condition.type.meta.DistanceFromCoordinatesMetaConditionType;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.Shape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

/**
 *	@author Alluysl (refactored by eggohito)
 */
public class DistanceFromCoordinatesBlockConditionType extends BlockConditionType implements DistanceFromCoordinatesMetaConditionType {

	private final Reference reference;
	private final Shape shape;

	private final Optional<Integer> roundToDigit;
	private final Vec3d offset;

	private final Comparison comparison;
	private final double compareTo;

	private final boolean scaleReferenceToDimension;
	private final boolean scaleDistanceToDimension;

	private final boolean ignoreX;
	private final boolean ignoreY;
	private final boolean ignoreZ;

	public DistanceFromCoordinatesBlockConditionType(DistanceFromCoordinatesMetaConditionType.Reference reference, Shape shape, Optional<Integer> roundToDigit, Vec3d offset, Comparison comparison, double compareTo, boolean scaleReferenceToDimension, boolean scaleDistanceToDimension, boolean ignoreX, boolean ignoreY, boolean ignoreZ) {
		this.reference = reference;
		this.shape = shape;
		this.roundToDigit = roundToDigit;
		this.offset = offset;
		this.comparison = comparison;
		this.compareTo = compareTo;
		this.scaleReferenceToDimension = scaleReferenceToDimension;
		this.scaleDistanceToDimension = scaleDistanceToDimension;
		this.ignoreX = ignoreX;
		this.ignoreY = ignoreY;
		this.ignoreZ = ignoreZ;
	}

	@Override
	public boolean test(World world, BlockPos pos) {
		return testCondition(Either.left(new BlockConditionContext(world, pos)));
	}

	@Override
	public ConditionConfiguration<?> configuration() {
		return BlockConditionTypes.DISTANCE_FROM_COORDINATES;
	}

	@Override
	public Reference reference() {
		return reference;
	}

	@Override
	public Shape shape() {
		return shape;
	}

	@Override
	public Optional<Integer> roundToDigit() {
		return roundToDigit;
	}

	@Override
	public Vec3d offset() {
		return offset;
	}

	@Override
	public Comparison comparison() {
		return comparison;
	}

	@Override
	public double compareTo() {
		return compareTo;
	}

	@Override
	public boolean scaleReferenceToDimension() {
		return scaleReferenceToDimension;
	}

	@Override
	public boolean scaleDistanceToDimension() {
		return scaleDistanceToDimension;
	}

	@Override
	public boolean ignoreX() {
		return ignoreX;
	}

	@Override
	public boolean ignoreY() {
		return ignoreY;
	}

	@Override
	public boolean ignoreZ() {
		return ignoreZ;
	}

}
