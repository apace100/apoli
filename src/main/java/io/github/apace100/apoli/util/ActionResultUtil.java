package io.github.apace100.apoli.util;

import net.minecraft.util.ActionResult;

public class ActionResultUtil {

    public static boolean shouldOverride(ActionResult oldResult, ActionResult newResult) {
        return (newResult.isAccepted() && !oldResult.isAccepted())
            || (newResult.shouldSwingHand() && !oldResult.shouldSwingHand());
    }

}
