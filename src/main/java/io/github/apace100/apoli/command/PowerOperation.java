package io.github.apace100.apoli.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.apace100.apoli.power.type.CooldownPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.power.type.VariableIntPowerType;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

// Very similar to OperationArgumentType, but modified to make it work with resources.
public class PowerOperation implements ArgumentType<PowerOperation.Operation> {
    public static final SimpleCommandExceptionType INVALID_OPERATION = new SimpleCommandExceptionType(Text.translatable("arguments.operation.invalid"));
    public static final SimpleCommandExceptionType DIVISION_ZERO_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("arguments.operation.div0"));

    public static PowerOperation operation() {
        return new PowerOperation();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, builder);
    }

    public PowerOperation.Operation parse(StringReader stringReader) throws CommandSyntaxException {
        if (!stringReader.canRead()) throw INVALID_OPERATION.create();

        int i = stringReader.getCursor();
        while(stringReader.canRead() && stringReader.peek() != ' ') stringReader.skip();

        String stringOperator = stringReader.getString().substring(i, stringReader.getCursor());
        return switch (stringOperator) {
            case "=" -> (power, score) -> {
                if (power instanceof VariableIntPowerType) {
                    ((VariableIntPowerType) power).setValue(score.getScore());
                } else if (power instanceof CooldownPowerType) {
                    ((CooldownPowerType) power).setCooldown(score.getScore());
                }
            };
            case "+=" -> (power, score) -> {
                if (power instanceof VariableIntPowerType) {
                    ((VariableIntPowerType) power).setValue(((VariableIntPowerType) power).getValue() + score.getScore());
                } else if (power instanceof CooldownPowerType) {
                    ((CooldownPowerType) power).modify(score.getScore());
                }
            };
            case "-=" -> (power, score) -> {
                if (power instanceof VariableIntPowerType) {
                    ((VariableIntPowerType) power).setValue(((VariableIntPowerType) power).getValue() - score.getScore());
                } else if (power instanceof CooldownPowerType) {
                    ((CooldownPowerType) power).modify(-score.getScore());
                }
            };
            case "*=" -> (power, score) -> {
                if (power instanceof VariableIntPowerType) {
                    ((VariableIntPowerType) power).setValue(((VariableIntPowerType) power).getValue() * score.getScore());
                } else if (power instanceof CooldownPowerType) {
                    ((CooldownPowerType) power).setCooldown(((CooldownPowerType) power).getRemainingTicks() * score.getScore());
                }
            };
            case "/=" -> (power, score) -> {
                if (power instanceof VariableIntPowerType resource) {
                    int r = resource.getValue();
                    int s = score.getScore();
                    if (s == 0) {
                        throw DIVISION_ZERO_EXCEPTION.create();
                    } else {
                        resource.setValue(Math.floorDiv(r, s));
                    }
                } else if (power instanceof CooldownPowerType cooldownPower) {
                    int c = cooldownPower.getRemainingTicks();
                    int s = score.getScore();
                    if (s == 0) {
                        throw DIVISION_ZERO_EXCEPTION.create();
                    } else {
                        cooldownPower.setCooldown(Math.floorDiv(c, s));
                    }
                }
            };
            case "%=" -> (power, score) -> {
                if (power instanceof VariableIntPowerType resource) {
                    int r = resource.getValue();
                    int s = score.getScore();
                    if (s == 0) {
                        throw DIVISION_ZERO_EXCEPTION.create();
                    } else {
                        resource.setValue(Math.floorMod(r, s));
                    }
                } else if (power instanceof CooldownPowerType cooldownPower) {
                    int c = cooldownPower.getRemainingTicks();
                    int s = score.getScore();
                    if (s == 0) {
                        throw DIVISION_ZERO_EXCEPTION.create();
                    } else {
                        cooldownPower.setCooldown(Math.floorMod(c, s));
                    }
                }
            };
            case "<" -> (power, score) -> {
                if (power instanceof VariableIntPowerType resource) {
                    resource.setValue(Math.min(resource.getValue(), score.getScore()));
                } else if (power instanceof CooldownPowerType cooldownPower) {
                    cooldownPower.setCooldown(Math.min(cooldownPower.getRemainingTicks(), score.getScore()));
                }
            };
            case ">" -> (power, score) -> {
                if (power instanceof VariableIntPowerType resource) {
                    resource.setValue(Math.max(resource.getValue(), score.getScore()));
                } else if (power instanceof CooldownPowerType cooldownPower) {
                    cooldownPower.setCooldown(Math.max(cooldownPower.getRemainingTicks(), score.getScore()));
                }
            };
            case "><" -> (power, score) -> {
                if (power instanceof VariableIntPowerType resource) {
                    int v = score.getScore();
                    score.setScore(resource.getValue());
                    resource.setValue(v);
                } else if (power instanceof CooldownPowerType cooldownPower) {
                    int v = score.getScore();
                    score.setScore(cooldownPower.getRemainingTicks());
                    cooldownPower.setCooldown(v);
                }
            };
            default -> throw INVALID_OPERATION.create();
        };
    }

    public interface Operation {
        void apply(PowerType powerType, ScoreAccess score) throws CommandSyntaxException;
    }
}
