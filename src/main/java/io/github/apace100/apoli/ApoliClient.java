package io.github.apace100.apoli;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.ModPackets;
import io.github.apace100.apoli.networking.ModPacketsS2C;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.condition.*;
import io.github.apace100.apoli.screen.GameHudRender;
import io.github.apace100.apoli.screen.PowerHudRenderer;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.github.apace100.apoli.util.ApoliConfigServer;
import io.netty.buffer.Unpooled;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.PacketByteBuf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ApoliClient implements ClientModInitializer {

	public static boolean shouldReloadWorldRenderer = false;

	private static HashMap<String, KeyBinding> idToKeyBindingMap = new HashMap<>();
	private static HashMap<String, Boolean> lastKeyBindingStates = new HashMap<>();
	private static boolean initializedKeyBindingMap = false;

	public static void registerPowerKeybinding(String keyId, KeyBinding keyBinding) {
		idToKeyBindingMap.put(keyId, keyBinding);
	}

	@Override
	public void onInitializeClient() {

		ModPacketsS2C.register();

		EntityConditionsClient.register();
		ClientTickEvents.START_CLIENT_TICK.register(tick -> {
			if(tick.player != null) {
				List<Power> powers = PowerHolderComponent.KEY.get(tick.player).getPowers();
				List<Power> pressedPowers = new LinkedList<>();
				HashMap<String, Boolean> currentKeyBindingStates = new HashMap<>();
				for(Power power : powers) {
					if(power instanceof Active) {
						Active active = (Active)power;
						Active.Key key = active.getKey();
						KeyBinding keyBinding = getKeyBinding(key.key);
						if(keyBinding != null) {
							if(!currentKeyBindingStates.containsKey(key.key)) {
								currentKeyBindingStates.put(key.key, keyBinding.isPressed());
							}
							if(currentKeyBindingStates.get(key.key) && (key.continuous || !lastKeyBindingStates.getOrDefault(key.key, false))) {
								pressedPowers.add(power);
							}
						}
					}
				}
				lastKeyBindingStates = currentKeyBindingStates;
				if(pressedPowers.size() > 0) {
					performActivePowers(pressedPowers);
				}
			}
		});

		GameHudRender.HUD_RENDERS.add(new PowerHudRenderer());

		AutoConfig.register(ApoliConfigClient.class, JanksonConfigSerializer::new);
		Apoli.config = AutoConfig.getConfigHolder(ApoliConfigClient.class).getConfig();
	}

	@Environment(EnvType.CLIENT)
	private void performActivePowers(List<Power> powers) {
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeInt(powers.size());
		for(Power power : powers) {
			buffer.writeIdentifier(power.getType().getIdentifier());
			((Active)power).onUse();
		}
		ClientPlayNetworking.send(ModPackets.USE_ACTIVE_POWERS, buffer);
	}

	@Environment(EnvType.CLIENT)
	private KeyBinding getKeyBinding(String key) {
		if(!idToKeyBindingMap.containsKey(key)) {
			if(!initializedKeyBindingMap) {
				initializedKeyBindingMap = true;
				MinecraftClient client = MinecraftClient.getInstance();
				for(int i = 0; i < client.options.keysAll.length; i++) {
					idToKeyBindingMap.put(client.options.keysAll[i].getTranslationKey(), client.options.keysAll[i]);
				}
				return getKeyBinding(key);
			}
			return null;
		}
		return idToKeyBindingMap.get(key);
	}
}
