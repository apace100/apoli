package io.github.apace100.apoli;

import io.github.apace100.apoli.networking.ModPacketsS2C;
import io.github.apace100.apoli.power.factory.condition.EntityConditionsClient;
import io.github.apace100.apoli.power.factory.condition.EntityConditionsServer;
import io.github.apace100.apoli.util.ApoliConfig;
import io.github.apace100.apoli.util.ApoliConfigServer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class ApoliServer implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {

		EntityConditionsServer.register();

		AutoConfig.register(ApoliConfigServer.class, JanksonConfigSerializer::new);
		Apoli.config = AutoConfig.getConfigHolder(ApoliConfigServer.class).getConfig();

	}
}
