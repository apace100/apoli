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

	public static <T, C extends AbstractCondition<T, CT>, CT extends AbstractConditionType<T, C>> Optional<C> setPowerType(Optional<C> condition, Optional<PowerType> powerType) {
		return condition.map(c -> {
			c.setPowerType(powerType);
			return c;
		});
	}

	public static <T, C extends AbstractCondition<T, CT>, CT extends AbstractConditionType<T, C>> Optional<C> setPowerType(Optional<C> condition, PowerType powerType) {
		return setPowerType(condition, Optional.ofNullable(powerType));
	}

	@Nullable
	public static <T, C extends AbstractCondition<T, CT>, CT extends AbstractConditionType<T, C>> C setPowerType(C condition, Optional<PowerType> powerType) {
		return setPowerType(Optional.ofNullable(condition), powerType).orElse(null);
	}

	@Nullable
	public static <T, C extends AbstractCondition<T, CT>, CT extends AbstractConditionType<T, C>> C setPowerType(C condition, PowerType powerType) {
		return setPowerType(Optional.ofNullable(condition), powerType).orElse(null);
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

	protected final void setPowerType(Optional<PowerType> powerType) {
		this.powerType = powerType;
	}

	public final CT getConditionType() {
		return conditionType;
	}

	public final boolean isInverted() {
		return inverted;
	}

}
