package io.github.apace100.apoli.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.PowerUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

// Very similar to OperationArgumentType, but modified to make it work with resources.
public class PowerOperationArgumentType implements ArgumentType<PowerOperationArgumentType.Operation> {

    public static final SimpleCommandExceptionType INVALID_OPERATION = new SimpleCommandExceptionType(Text.translatable("arguments.operation.invalid"));
    public static final SimpleCommandExceptionType DIVISION_ZERO_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("arguments.operation.div0"));

    public static PowerOperationArgumentType operation() {
        return new PowerOperationArgumentType();
    }

    public static Operation getOperation(CommandContext<ServerCommandSource> context, String argumentName) {
        return context.getArgument(argumentName, Operation.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, builder);
    }

    @Override
    public PowerOperationArgumentType.Operation parse(StringReader stringReader) throws CommandSyntaxException {

        if (!stringReader.canRead()) {
            throw INVALID_OPERATION.create();
        }

        int i = stringReader.getCursor();
        while (stringReader.canRead() && stringReader.peek() != ' ') {
            stringReader.skip();
        }

        String stringOperator = stringReader.getString().substring(i, stringReader.getCursor());
        return switch (stringOperator) {
            case "=" -> (powerType, scoreAccess) ->
                PowerUtil.setResourceValue(powerType, scoreAccess.getScore());
            case "+=" -> (powerType, scoreAccess) ->
                PowerUtil.changeResourceValue(powerType, scoreAccess.getScore());
            case "-=" -> (powerType, scoreAccess) ->
                PowerUtil.changeResourceValue(powerType, -scoreAccess.getScore());
            case "*=" -> (powerType, scoreAccess) ->
                PowerUtil.setResourceValue(powerType, PowerUtil.getResourceValue(powerType) * scoreAccess.getScore());
            case "/=" -> (powerType, scoreAccess) -> {

                int resourceValue = PowerUtil.getResourceValue(powerType);
                int scoreValue = scoreAccess.getScore();

                if (scoreValue == 0) {
                    throw DIVISION_ZERO_EXCEPTION.create();
                }

                else {
                    return PowerUtil.setResourceValue(powerType, Math.floorDiv(resourceValue, scoreValue));
                }

            };
            case "%=" -> (powerType, scoreAccess) -> {

                int resourceValue = PowerUtil.getResourceValue(powerType);
                int scoreValue = scoreAccess.getScore();

                if (scoreValue == 0) {
                    throw DIVISION_ZERO_EXCEPTION.create();
                }

                else {
                    return PowerUtil.setResourceValue(powerType, Math.floorMod(resourceValue, scoreValue));
                }

            };
            case "<" -> (powerType, scoreAccess) ->
                PowerUtil.setResourceValue(powerType, Math.min(PowerUtil.getResourceValue(powerType), scoreAccess.getScore()));
            case ">" -> (powerType, scoreAccess) ->
                PowerUtil.setResourceValue(powerType, Math.max(PowerUtil.getResourceValue(powerType), scoreAccess.getScore()));
            case "><" -> (powerType, scoreAccess) -> {

                int resourceValue = PowerUtil.getResourceValue(powerType);
                int scoreValue = scoreAccess.getScore();

                scoreAccess.setScore(resourceValue);
                return PowerUtil.setResourceValue(powerType, scoreValue);

            };
            default ->
                throw INVALID_OPERATION.create();
        };
    }

    public interface Operation {
        boolean apply(PowerType powerType, ScoreAccess scoreAccess) throws CommandSyntaxException;
    }

}
