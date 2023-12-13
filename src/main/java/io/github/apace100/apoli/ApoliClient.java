package io.github.apace100.apoli;

import io.github.apace100.apoli.integration.PowerIntegrationClient;
import io.github.apace100.apoli.networking.ModPacketsS2C;
import io.github.apace100.apoli.networking.packet.c2s.UseActivePowersC2SPacket;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.registry.ApoliClassDataClient;
import io.github.apace100.apoli.screen.GameHudRender;
import io.github.apace100.apoli.screen.PowerHudRenderer;
import io.github.apace100.apoli.util.ApoliConfigClient;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ApoliClient implements ClientModInitializer {

	public static KeyBinding showPowersOnUsabilityHint;

	public static final HashMap<String, KeyBinding> idToKeyBindingMap = new HashMap<>();
	public static final HashMap<String, Boolean> lastKeyBindingStates = new HashMap<>();

	private static boolean initializedKeyBindingMap = false;
	public static boolean shouldReloadWorldRenderer = false;

	public static void registerPowerKeybinding(String keyId, KeyBinding keyBinding) {
		idToKeyBindingMap.put(keyId, keyBinding);
	}

	@Override
	public void onInitializeClient() {

		showPowersOnUsabilityHint = new KeyBinding("key.apoli.usability_hint.show_powers", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category." + Apoli.MODID);
		KeyBindingHelper.registerKeyBinding(showPowersOnUsabilityHint);

		ModPacketsS2C.register();

		ApoliClassDataClient.registerAll();
		PowerIntegrationClient.register();

		GameHudRender.HUD_RENDERS.add(new PowerHudRenderer());

		AutoConfig.register(ApoliConfigClient.class, JanksonConfigSerializer::new);
		Apoli.config = AutoConfig.getConfigHolder(ApoliConfigClient.class).getConfig();
	}

	public static void performActivePowers(List<Power> powers) {

		List<Identifier> powerTypeIds = new LinkedList<>();
		for (Power power : powers) {

			if (power instanceof Active activePower) {
				activePower.onUse();
			}

			powerTypeIds.add(power.getType().getIdentifier());

		}

		ClientPlayNetworking.send(new UseActivePowersC2SPacket(powerTypeIds));

	}

	public static KeyBinding getKeyBinding(String key) {

		if (idToKeyBindingMap.containsKey(key)) {
			return idToKeyBindingMap.get(key);
		}

		if (initializedKeyBindingMap) {
			return null;
		}

		for (KeyBinding keyBinding : MinecraftClient.getInstance().options.allKeys) {
			idToKeyBindingMap.put(keyBinding.getTranslationKey(), keyBinding);
		}

		initializedKeyBindingMap = true;
		return getKeyBinding(key);

	}

}
