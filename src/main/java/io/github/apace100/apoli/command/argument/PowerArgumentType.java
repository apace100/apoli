package io.github.apace100.apoli.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class PowerArgumentType implements ArgumentType<Power> {

    public static final DynamicCommandExceptionType POWER_NOT_FOUND = new DynamicCommandExceptionType(
        o -> Text.stringifiedTranslatable("commands.apoli.power_not_found", o)
    );

    protected PowerArgumentType() {

    }

    public static PowerArgumentType power() {
        return new PowerArgumentType();
    }

    public static Power getPower(CommandContext<ServerCommandSource> context, String argumentName) {
        return context.getArgument(argumentName, Power.class);
    }

    @Override
    public Power parse(StringReader reader) throws CommandSyntaxException {
        Identifier id = Identifier.fromCommandInputNonEmpty(reader);
        return PowerManager
            .getOptional(id)
            .orElseThrow(() -> POWER_NOT_FOUND.create(id));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestIdentifiers(PowerManager.keySet().stream(), builder);
    }

}
