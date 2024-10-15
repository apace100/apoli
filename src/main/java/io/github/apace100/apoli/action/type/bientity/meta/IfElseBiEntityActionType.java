package io.github.apace100.apoli.action.type.bientity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.action.type.meta.IfElseMetaActionType;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.context.BiEntityConditionContext;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class IfElseBiEntityActionType extends BiEntityActionType implements IfElseMetaActionType<BiEntityActionContext, BiEntityConditionContext, BiEntityAction, BiEntityCondition> {

	private final BiEntityCondition condition;

	private final BiEntityAction ifAction;
	private final Optional<BiEntityAction> elseAction;

	public IfElseBiEntityActionType(BiEntityCondition condition, BiEntityAction ifAction, Optional<BiEntityAction> elseAction) {
		this.condition = condition;
		this.ifAction = ifAction;
		this.elseAction = elseAction;
	}

	@Override
	public void execute(Entity actor, Entity target) {
		executeAction(new BiEntityActionContext(actor, target));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BiEntityActionTypes.IF_ELSE;
	}

	@Override
	public BiEntityCondition condition() {
		return condition;
	}

	@Override
	public BiEntityAction ifAction() {
		return ifAction;
	}

	@Override
	public Optional<BiEntityAction> elseAction() {
		return elseAction;
	}

}
