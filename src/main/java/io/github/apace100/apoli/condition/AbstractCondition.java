package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.type.AbstractConditionType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.calio.util.Validatable;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractCondition<T, CT extends AbstractConditionType<T, ?>> implements Predicate<T>, Validatable {

	private final CT conditionType;
	private final boolean inverted;

	private Optional<PowerType> powerType;

	public AbstractCondition(CT conditionType, boolean inverted) {

		this.conditionType = conditionType;
		this.inverted = inverted;

		//noinspection unchecked
		((AbstractConditionType<T, AbstractCondition<T, CT>>) this.conditionType).setCondition(this);

	}

	@Nullable
	public static <T, C extends AbstractCondition<T, CT>, CT extends AbstractConditionType<T, C>> C setPowerType(@Nullable C condition, Optional<PowerType> powerType) {

		if (condition != null) {
			condition.setPowerType(powerType);
		}

		return condition;

	}

	@Nullable
	public static <T, C extends AbstractCondition<T, CT>, CT extends AbstractConditionType<T, C>> C setPowerType(@Nullable C condition, PowerType powerType) {

		if (condition != null) {
			condition.setPowerType(powerType);
		}

		return condition;

	}

	@Override
	public boolean test(T operand) {
		return isInverted() != getConditionType().test(operand);
	}

	@Override
	public void validate() throws Exception {
		getConditionType().validate();
	}

	public final Optional<PowerType> getPowerType() {
		return powerType;
	}

	public final void setPowerType(Optional<PowerType> powerType) {
		this.powerType = powerType;
	}

	public final void setPowerType(PowerType powerType) {
		setPowerType(Optional.ofNullable(powerType));
	}

	public final CT getConditionType() {
		return conditionType;
	}

	public final boolean isInverted() {
		return inverted;
	}

}
