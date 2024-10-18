package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.context.EntityConditionContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.entity.Entity;

public class EntityCondition extends AbstractCondition<EntityConditionContext, EntityConditionType> {

	public static final SerializableDataType<EntityCondition> DATA_TYPE = SerializableDataType.lazy(() -> ApoliDataTypes.condition("type", EntityConditionTypes.DATA_TYPE, EntityCondition::new));

	public EntityCondition(EntityConditionType conditionType, boolean inverted) {
		super(conditionType, inverted);
	}

	public EntityCondition(EntityConditionType conditionType) {
		this(conditionType, false);
	}

	public boolean test(Entity entity) {
		return test(new EntityConditionContext(entity));
	}

}
