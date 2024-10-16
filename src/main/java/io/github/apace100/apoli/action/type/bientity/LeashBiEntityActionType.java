package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.access.CustomLeashable;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;

public class LeashBiEntityActionType extends BiEntityActionType {

    @Override
	protected void execute(Entity actor, Entity target) {

        if (actor == null || !(target instanceof Leashable leashable) || !(target instanceof CustomLeashable customLeashable)) {
            return;
        }

        if (!leashable.isLeashed()) {
            customLeashable.apoli$setCustomLeashed(true);
            leashable.attachLeash(actor, true);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BiEntityActionTypes.LEASH;
    }

}
