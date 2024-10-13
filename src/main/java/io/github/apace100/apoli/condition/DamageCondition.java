package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.context.DamageContext;
import io.github.apace100.apoli.condition.type.DamageConditionType;
import io.github.apace100.apoli.condition.type.DamageConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import net.minecraft.entity.damage.DamageSource;

public class DamageCondition extends AbstractCondition<DamageContext, DamageConditionType> {

	public static final CompoundSerializableDataType<DamageCondition> DATA_TYPE = ApoliDataTypes.condition("type", DamageConditionTypes.DATA_TYPE, DamageCondition::new);

	public DamageCondition(DamageConditionType conditionType, boolean inverted) {
		super(conditionType, inverted);
	}

	public DamageCondition(DamageConditionType conditionType) {
		this(conditionType, false);
	}

	public boolean test(DamageSource source, float amount) {
		return test(new DamageContext(source, amount));
	}

}
