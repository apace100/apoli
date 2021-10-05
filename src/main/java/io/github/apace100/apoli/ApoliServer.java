package io.github.apace100.apoli;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnBeingUsedPower;
import io.github.apace100.apoli.power.ActionOnEntityUsePower;
import io.github.apace100.apoli.power.PreventBeingUsedPower;
import io.github.apace100.apoli.power.PreventEntityUsePower;
import io.github.apace100.apoli.power.factory.condition.EntityConditionsServer;
import io.github.apace100.apoli.util.ApoliConfigServer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

import java.util.List;
import java.util.stream.Collectors;

public class ApoliServer implements DedicatedServerModInitializer {

	@Override
	public void onInitializeServer() {

		EntityConditionsServer.register();

		AutoConfig.register(ApoliConfigServer.class, JanksonConfigSerializer::new);
		Apoli.config = AutoConfig.getConfigHolder(ApoliConfigServer.class).getConfig();

		UseEntityCallback.EVENT.register(((playerEntity, world, hand, entity, entityHitResult) -> {
			if(playerEntity.isSpectator()) {
				return ActionResult.PASS;
			}
			ItemStack stack = playerEntity.getStackInHand(hand);
			for(PreventEntityUsePower peup : PowerHolderComponent.getPowers(playerEntity, PreventEntityUsePower.class)) {
				if(peup.doesApply(entity, hand, stack)) {
					return peup.executeAction(entity, hand);
				}
			}
			for(PreventBeingUsedPower pbup : PowerHolderComponent.getPowers(entity, PreventBeingUsedPower.class)) {
				if(pbup.doesApply(playerEntity, hand, stack)) {
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

	}
}
