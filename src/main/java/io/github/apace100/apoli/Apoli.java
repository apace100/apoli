package io.github.apace100.apoli;

import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.command.PowerCommand;
import io.github.apace100.apoli.command.ResourceCommand;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.component.PowerHolderComponentImpl;
import io.github.apace100.apoli.component.item.ApoliDataComponentTypes;
import io.github.apace100.apoli.component.item.ItemPowersComponent;
import io.github.apace100.apoli.condition.type.*;
import io.github.apace100.apoli.data.ApoliDataHandlers;
import io.github.apace100.apoli.global.GlobalPowerSetManager;
import io.github.apace100.apoli.integration.PowerIntegration;
import io.github.apace100.apoli.loot.condition.ApoliLootConditionTypes;
import io.github.apace100.apoli.loot.function.ApoliLootFunctionTypes;
import io.github.apace100.apoli.networking.ModPackets;
import io.github.apace100.apoli.networking.ModPacketsC2S;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.power.type.PowerTypes;
import io.github.apace100.apoli.recipe.ApoliRecipeSerializers;
import io.github.apace100.apoli.registry.ApoliClassData;
import io.github.apace100.apoli.util.ApoliConfig;
import io.github.apace100.apoli.util.GainedPowerCriterion;
import io.github.apace100.apoli.util.modifier.ModifierOperations;
import io.github.apace100.calio.Calio;
import io.github.apace100.calio.util.CalioResourceConditions;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class Apoli implements ModInitializer, EntityComponentInitializer {

	public static ApoliConfig config;
	public static MinecraftServer server;

	public static final String MODID = "apoli";
	public static final Logger LOGGER = LogManager.getLogger(Apoli.class);

	public static String VERSION = "";
	public static int[] SEMVER;

	public static final AbilitySource LEGACY_POWER_SOURCE = Pal.getAbilitySource(Apoli.identifier("power_source"));

	public static final boolean PERFORM_VERSION_CHECK = false;

	@Override
	public void onInitialize() {

		ServerLifecycleEvents.SERVER_STARTING.register(server -> Apoli.server = server);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> Apoli.server = null);

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

		ModPackets.register();
		ModPacketsC2S.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			PowerCommand.register(dispatcher.getRoot());
			ResourceCommand.register(dispatcher.getRoot());
		});

		ApoliLootFunctionTypes.register();
		ApoliLootConditionTypes.register();

		ApoliClassData.registerAll();

		ModifierOperations.registerAll();
		ApoliDataComponentTypes.register();
		ApoliRecipeSerializers.register();

		BiEntityConditionTypes.register();
		BiomeConditionTypes.register();
		BlockConditionTypes.register();
		DamageConditionTypes.register();
		EntityConditionTypes.register();
		FluidConditionTypes.register();
		ItemConditionTypes.register();

		EntityActionTypes.register();
		ItemActionTypes.register();
		BlockActionTypes.register();
		BiEntityActionTypes.register();

		PowerTypes.register();
		PowerIntegration.register();

		ApoliDataHandlers.register();

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new PowerManager());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new GlobalPowerSetManager());

		ServerEntityEvents.EQUIPMENT_CHANGE.register(ItemPowersComponent::onChangeEquipment);

		CalioResourceConditions.ALIASES.addNamespaceAlias(MODID, Calio.MOD_NAMESPACE);
		Criteria.register(GainedPowerCriterion.ID.toString(), GainedPowerCriterion.INSTANCE);

		LOGGER.info("Apoli " + VERSION + " has initialized. Ready to power up your game!");

	}

	public static Identifier identifier(String path) {
		return Identifier.of(MODID, path);
	}

	public static boolean onServerSide() {
		return server != null && server.isOnThread();
	}

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.beginRegistration(LivingEntity.class, PowerHolderComponent.KEY)
			.impl(PowerHolderComponentImpl.class)
			.respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY)
			.end(PowerHolderComponentImpl::new);
	}

}
