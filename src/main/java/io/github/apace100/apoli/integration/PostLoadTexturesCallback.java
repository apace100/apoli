package io.github.apace100.apoli.integration;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public interface PostLoadTexturesCallback {

    Event<PostLoadTexturesCallback> EVENT = EventFactory.createArrayBacked(
        PostLoadTexturesCallback.class,
        callbacks -> client -> {

            for (PostLoadTexturesCallback callback : callbacks) {
                callback.onPostLoad(client);
            }

        }
    );

    void onPostLoad(MinecraftClient client);

}
