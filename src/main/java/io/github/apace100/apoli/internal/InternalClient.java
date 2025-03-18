package io.github.apace100.apoli.internal;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

/**
 * @author Ampflower
 **/
@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public final class InternalClient {

    public static void onClientWorldChanged() {
        Internal.globalStateCleanup();
    }
}
