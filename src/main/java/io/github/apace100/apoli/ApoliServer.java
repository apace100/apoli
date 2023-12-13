package io.github.apace100.apoli;

import io.github.apace100.apoli.util.ApoliConfigServer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.DedicatedServerModInitializer;

public class ApoliServer implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {

		AutoConfig.register(ApoliConfigServer.class, JanksonConfigSerializer::new);
		Apoli.config = AutoConfig.getConfigHolder(ApoliConfigServer.class).getConfig();

	}
}
