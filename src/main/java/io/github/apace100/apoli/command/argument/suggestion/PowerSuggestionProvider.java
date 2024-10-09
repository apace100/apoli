package io.github.apace100.apoli.command.argument.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.command.argument.PowerArgumentType;
import io.github.apace100.apoli.command.argument.PowerHolderArgumentType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.github.apace100.apoli.util.PowerUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

public record PowerSuggestionProvider(Function<CommandContext<ServerCommandSource>, Collection<Entity>> getter, PowerArgumentType.PowerTarget targetType) implements SuggestionProvider<ServerCommandSource> {

	private static PowerSuggestionProvider entity(String entityArgumentName, PowerArgumentType.PowerTarget targetType) {
		return new PowerSuggestionProvider(context -> {

			try {
				return List.of(PowerHolderArgumentType.getHolder(context, entityArgumentName));
			}

			catch (IllegalArgumentException iae) {
				Apoli.LOGGER.warn("Something went wrong trying to get an entity from argument \"{}\": ", entityArgumentName, iae);
				throw iae;
			}

			catch (CommandSyntaxException e) {
				throw new IllegalStateException(e);
			}

		}, targetType);
	}

	private static PowerSuggestionProvider entities(String entitiesArgumentName, PowerArgumentType.PowerTarget targetType) {
		return new PowerSuggestionProvider(context -> {

			try {
				return PowerHolderArgumentType.getHolders(context, entitiesArgumentName)
					.stream()
					.map(Entity.class::cast)
					.toList();
			}

			catch (IllegalArgumentException iae) {
				Apoli.LOGGER.warn("Something went wrong trying to get entities from argument \"{}\": ", entitiesArgumentName, iae);
				throw iae;
			}

			catch (CommandSyntaxException e) {
				throw new IllegalStateException(e);
			}

		}, targetType);
	}

	public static PowerSuggestionProvider powersFromEntity(String entityArgumentName) {
		return entity(entityArgumentName, PowerArgumentType.PowerTarget.GENERAL);
	}

	public static PowerSuggestionProvider powersFromEntities(String entitiesArgumentName) {
		return entities(entitiesArgumentName, PowerArgumentType.PowerTarget.GENERAL);
	}

	public static PowerSuggestionProvider resourcesFromEntity(String entityArgumentName) {
		return entity(entityArgumentName, PowerArgumentType.PowerTarget.RESOURCE);
	}

	public static PowerSuggestionProvider resourcesFromEntities(String entitiesArgumentName) {
		return entities(entitiesArgumentName, PowerArgumentType.PowerTarget.RESOURCE);
	}

	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {

		try {

			Stream.Builder<Identifier> powerIds = Stream.builder();
			Collection<Entity> entities = getter().apply(context);

			for (Entity entity : entities) {

				PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(entity);
				for (Map.Entry<Identifier, Power> powerEntry : PowerManager.entrySet()) {

					Identifier id = powerEntry.getKey();
					Power power = powerEntry.getValue();

					try {

						if (powerComponent.hasPower(power) && isAllowed(power)) {
							powerIds.add(id);
						}

					}

					catch (Exception e) {
						Apoli.LOGGER.error("Error trying to put power \"{}\" in the suggestion provider (skipping): {}", id, e);
					}

				}

			}

			return CommandSource.suggestIdentifiers(powerIds.build(), builder);

		}

		catch (Exception e) {
			return Suggestions.empty();
		}

	}

	private boolean isAllowed(Power power) {
		return targetType() != PowerArgumentType.PowerTarget.RESOURCE
			|| PowerUtil.validateResource(power.create(null)).isSuccess();
	}

}
