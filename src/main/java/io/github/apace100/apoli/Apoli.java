package io.github.apace100.apoli;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import io.github.apace100.apoli.command.PowerCommand;
import io.github.apace100.apoli.command.ResourceCommand;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.component.PowerHolderComponentImpl;
import io.github.apace100.apoli.global.GlobalPowerSetLoader;
import io.github.apace100.apoli.networking.ModPacketsC2S;
import io.github.apace100.apoli.power.PowerTypes;
import io.github.apace100.apoli.power.factory.PowerFactories;
import io.github.apace100.apoli.power.factory.action.BiEntityActions;
import io.github.apace100.apoli.power.factory.action.BlockActions;
import io.github.apace100.apoli.power.factory.action.EntityActions;
import io.github.apace100.apoli.power.factory.action.ItemActions;
import io.github.apace100.apoli.power.factory.condition.*;
import io.github.apace100.apoli.registry.ApoliClassData;
import io.github.apace100.apoli.util.*;
import io.github.apace100.apoli.util.modifier.ModifierOperations;
import io.github.apace100.calio.mixin.CriteriaRegistryInvoker;
import io.github.apace100.calio.resource.OrderedResourceListenerInitializer;
import io.github.apace100.calio.resource.OrderedResourceListenerManager;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Apoli implements ModInitializer, EntityComponentInitializer, OrderedResourceListenerInitializer {

	public static ApoliConfig config;

	public static MinecraftServer server;

	public static final Scheduler SCHEDULER = new Scheduler();

	public static final String MODID = "apoli";
	public static final Logger LOGGER = LogManager.getLogger(Apoli.class);
	public static String VERSION = "";
	public static int[] SEMVER;

	public static final AbilitySource LEGACY_POWER_SOURCE = Pal.getAbilitySource(Apoli.identifier("power_source"));

	public static final boolean PERFORM_VERSION_CHECK = false;

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);

		FabricLoader.getInstance().getModContainer(MODID).ifPresent(modContainer -> {
			VERSION = modContainer.getMetadata().getVersion().getFriendlyString();
			if(VERSION.contains("+")) {
				VERSION = VERSION.split("\\+")[0];
			}
			if(VERSION.contains("-")) {
				VERSION = VERSION.split("-")[0];
			}
			String[] splitVersion = VERSION.split("\\.");
			SEMVER = new int[splitVersion.length];
			for(int i = 0; i < SEMVER.length; i++) {
				SEMVER[i] = Integer.parseInt(splitVersion[i]);
			}
		});

		ModPacketsC2S.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			PowerCommand.register(dispatcher);
			ResourceCommand.register(dispatcher);
		});

		Registry.register(Registries.RECIPE_SERIALIZER, Apoli.identifier("power_restricted"), PowerRestrictedCraftingRecipe.SERIALIZER);
		Registry.register(Registries.RECIPE_SERIALIZER, Apoli.identifier("modified"), ModifiedCraftingRecipe.SERIALIZER);

		Registry.register(Registries.LOOT_FUNCTION_TYPE, Apoli.identifier("add_power"), AddPowerLootFunction.TYPE);
		Registry.register(Registries.LOOT_FUNCTION_TYPE, Apoli.identifier("remove_power"), RemovePowerLootFunction.TYPE);

		Registry.register(Registries.LOOT_CONDITION_TYPE, Apoli.identifier("power"), PowerLootCondition.TYPE);

		ApoliClassData.registerAll();

		ModifierOperations.registerAll();

		PowerFactories.register();
		EntityConditions.register();
		BiEntityConditions.register();
		ItemConditions.register();
		BlockConditions.register();
		DamageConditions.register();
		FluidConditions.register();
		BiomeConditions.register();
		EntityActions.register();
		ItemActions.register();
		BlockActions.register();
		BiEntityActions.register();

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new GlobalPowerSetLoader());
		ResourceConditions.register(ApoliResourceConditions.ANY_NAMESPACE_LOADED, jsonObject -> ApoliResourceConditions.namespacesLoaded(jsonObject, PowerTypes.LOADED_NAMESPACES, false));
		ResourceConditions.register(ApoliResourceConditions.ALL_NAMESPACES_LOADED, jsonObject -> ApoliResourceConditions.namespacesLoaded(jsonObject, PowerTypes.LOADED_NAMESPACES, true));

		CriteriaRegistryInvoker.callRegister(GainedPowerCriterion.INSTANCE);

		LOGGER.info("Apoli " + VERSION + " has initialized. Ready to power up your game!");
	}

	public static Identifier identifier(String path) {
		return new Identifier(MODID, path);
	}

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.beginRegistration(LivingEntity.class, PowerHolderComponent.KEY)
			.impl(PowerHolderComponentImpl.class)
			.respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY)
			.end(PowerHolderComponentImpl::new);
	}

	@Override
	public void registerResourceListeners(OrderedResourceListenerManager manager) {
		manager.register(ResourceType.SERVER_DATA, new PowerTypes()).complete();
	}
}
