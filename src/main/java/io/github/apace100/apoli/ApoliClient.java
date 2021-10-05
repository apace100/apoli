package io.github.apace100.apoli;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.ModPackets;
import io.github.apace100.apoli.networking.ModPacketsS2C;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.power.factory.condition.EntityConditionsClient;
import io.github.apace100.apoli.screen.GameHudRender;
import io.github.apace100.apoli.screen.PowerHudRenderer;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.netty.buffer.Unpooled;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

		UseEntityCallback.EVENT.register(((playerEntity, world, hand, entity, entityHitResult) -> {
			if(playerEntity.isSpectator()) {
				return ActionResult.PASS;
			}
			ItemStack stack = playerEntity.getStackInHand(hand);
			for(PreventEntityUsePower peup : PowerHolderComponent.getPowers(playerEntity, PreventEntityUsePower.class)) {
				if(peup.doesApply(entity, hand, stack)) {
					PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
					buf.writeInt(entity.getId());
					buf.writeInt(hand.ordinal());
					ClientPlayNetworking.send(ModPackets.PREVENTED_ENTITY_USE, buf);
					return peup.executeAction(entity, hand);
				}
			}
			for(PreventBeingUsedPower pbup : PowerHolderComponent.getPowers(entity, PreventBeingUsedPower.class)) {
				if(pbup.doesApply(playerEntity, hand, stack)) {
					PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
					buf.writeInt(entity.getId());
					buf.writeInt(hand.ordinal());
					ClientPlayNetworking.send(ModPackets.PREVENTED_ENTITY_USE, buf);
					return pbup.executeAction(playerEntity, hand);
				}
			}
			ActionResult result = ActionResult.PASS;
			List<ActionOnEntityUsePower> powers = PowerHolderComponent.getPowers(playerEntity, ActionOnEntityUsePower.class).stream().filter(p -> p.shouldExecute(entity, hand, stack)).toList();
			for (ActionOnEntityUsePower aoip : powers) {
				ActionResult ar = aoip.executeAction(entity, hand);
				if(ar.isAccepted() && !result.isAccepted()) {
					result = ar;
				} else if(ar.shouldSwingHand() && !result.shouldSwingHand()) {
					result = ar;
				}
			}
			List<ActionOnBeingUsedPower> otherPowers = PowerHolderComponent.getPowers(entity, ActionOnBeingUsedPower.class).stream()
				.filter(p -> p.shouldExecute(playerEntity, hand, stack)).collect(Collectors.toList());
			for(ActionOnBeingUsedPower awip : otherPowers) {
				ActionResult ar = awip.executeAction(playerEntity, hand);
				if(ar.isAccepted() && !result.isAccepted()) {
					result = ar;
				} else if(ar.shouldSwingHand() && !result.shouldSwingHand()) {
					result = ar;
				}
			}
			if(powers.size() > 0 || otherPowers.size() > 0) {
				return result;
			}
			return ActionResult.PASS;
		}));

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
