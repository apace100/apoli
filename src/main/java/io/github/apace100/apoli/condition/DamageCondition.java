package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.context.DamageConditionContext;
import io.github.apace100.apoli.condition.type.DamageConditionType;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.damage.DamageSource;

public class DamageCondition extends AbstractCondition<DamageConditionContext, DamageConditionType> {

	public static final SerializableDataType<DamageCondition> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.condition("type", DamageConditionTypes.DATA_TYPE, DamageCondition::new));

	public DamageCondition(DamageConditionType conditionType, boolean inverted) {
		super(conditionType, inverted);
	}

	public DamageCondition(DamageConditionType conditionType) {
		this(conditionType, false);
	}

	public boolean test(DamageSource source, float amount) {
		return test(new DamageConditionContext(source, amount));
	}

}
