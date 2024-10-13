package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.context.BiEntityContext;
import io.github.apace100.apoli.condition.type.BiEntityConditionType;
import io.github.apace100.apoli.condition.type.BiEntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import net.minecraft.entity.Entity;

public final class BiEntityCondition extends AbstractCondition<BiEntityContext, BiEntityConditionType> {

	public static final CompoundSerializableDataType<BiEntityCondition> DATA_TYPE = ApoliDataTypes.condition("type", BiEntityConditionTypes.DATA_TYPE, BiEntityCondition::new);

	public BiEntityCondition(BiEntityConditionType conditionType, boolean inverted) {
		super(conditionType, inverted);
	}

	public BiEntityCondition(BiEntityConditionType conditionType) {
		this(conditionType, false);
	}

	public boolean test(Entity actor, Entity target) {
		return test(new BiEntityContext(actor, target));
	}

}
