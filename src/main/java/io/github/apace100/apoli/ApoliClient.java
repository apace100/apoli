package io.github.apace100.apoli;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.component.PowerHolderComponentImpl;
import io.github.apace100.apoli.networking.ModPacketsS2C;
import io.github.apace100.apoli.power.factory.condition.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ApoliClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {

		ModPacketsS2C.register();

		EntityConditionsClient.register();

	}
}
