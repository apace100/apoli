package io.github.apace100.apoli.action.type.bientity.meta;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.action.context.BiEntityActionContext;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.action.type.meta.ChanceMetaActionType;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class ChanceBiEntityActionType extends BiEntityActionType implements ChanceMetaActionType<BiEntityActionContext, BiEntityAction> {

	private final BiEntityAction successAction;
	private final Optional<BiEntityAction> failAction;

	private final float chance;

	public ChanceBiEntityActionType(BiEntityAction successAction, Optional<BiEntityAction> failAction, float chance) {
		this.successAction = successAction;
		this.failAction = failAction;
		this.chance = chance;
	}

	@Override
	protected void execute(Entity actor, Entity target) {
		executeAction(new BiEntityActionContext(actor, target));
	}

	@Override
	public ActionConfiguration<?> configuration() {
		return BiEntityActionTypes.CHANCE;
	}

	@Override
	public BiEntityAction successAction() {
		return successAction;
	}

	@Override
	public Optional<BiEntityAction> failAction() {
		return failAction;
	}

	@Override
	public float chance() {
		return chance;
	}

}
