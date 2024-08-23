package io.github.apace100.apoli.action.type.bientity;

import io.github.apace100.apoli.access.CustomLeashable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;

public class LeashActionType {

    public static void action(Entity actor, Entity target) {

        if (actor == null || !(target instanceof Leashable leashable) || !(target instanceof CustomLeashable customLeashable)) {
            return;
        }

        if (leashable.isLeashed()) {
            customLeashable.apoli$setCustomLeashed(true);
            leashable.attachLeash(actor, true);
        }

    }

}
