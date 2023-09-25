package io.github.apace100.apoli.screen;

import io.github.apace100.apoli.Apoli;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ApoliScreenHandlerTypes {

    public static ScreenHandlerType<DynamicContainerScreenHandler> DYNAMIC_CONTAINER;

    public static void registerAll() {
        DYNAMIC_CONTAINER = register("dynamic_container", DynamicContainerScreenHandler::new);
    }

    public static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType.Factory<T> factory) {
        return Registry.register(Registries.SCREEN_HANDLER, Apoli.identifier(id), new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
    }

}
