package io.github.apace100.apoli.condition;

import io.github.apace100.apoli.condition.context.EntityContext;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import net.minecraft.entity.Entity;

public class EntityCondition extends AbstractCondition<EntityContext, EntityConditionType> {

	public static final CompoundSerializableDataType<EntityCondition> DATA_TYPE = ApoliDataTypes.condition("type", EntityConditionTypes.DATA_TYPE, EntityCondition::new);

	public EntityCondition(EntityConditionType conditionType, boolean inverted) {
		super(conditionType, inverted);
	}

	public EntityCondition(EntityConditionType conditionType) {
		this(conditionType, false);
	}

	public boolean test(Entity entity) {
		return test(new EntityContext(entity));
	}

}
