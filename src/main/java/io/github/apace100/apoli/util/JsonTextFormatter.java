package io.github.apace100.apoli.util;

import com.google.common.base.Strings;
import com.google.gson.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class JsonTextFormatter {

    private static final Formatting NAME_COLOR = Formatting.AQUA;
    private static final Formatting STRING_COLOR = Formatting.GREEN;
    private static final Formatting NUMBER_COLOR = Formatting.GOLD;
    private static final Formatting BOOLEAN_COLOR = Formatting.BLUE;
    private static final Formatting TYPE_SUFFIX_COLOR = Formatting.RED;

    private final String indent;

    private final boolean root;
    private final int offset;

    protected JsonTextFormatter(String indent, int offset, boolean root) {
        this.indent = indent;
        this.offset = Math.max(0, offset);
        this.root = root;
    }

    public JsonTextFormatter(char indent, int size) {
        this(Strings.repeat(String.valueOf(indent), size), 1, true);
    }

    public JsonTextFormatter(int size) {
        this(' ', size);
    }

    public Text apply(JsonElement jsonElement) {
        return applyInternal(jsonElement).orElse(Text.empty());
    }

    protected Optional<Text> applyInternal(JsonElement jsonElement) {

        Text result = switch (jsonElement) {
            case JsonArray jsonArray ->
                visitArray(jsonArray);
            case JsonObject jsonObject ->
                visitObject(jsonObject);
            case JsonPrimitive jsonPrimitive ->
                visitPrimitive(jsonPrimitive);
            case JsonNull ignored ->
                null;
            case null ->
                throw new JsonSyntaxException("JSON element cannot be null!");
            default ->
                throw new JsonParseException("The format of JSON element " + jsonElement + " is not supported!");
        };

        return Optional.ofNullable(result);

    }

    public Text visitArray(JsonArray jsonArray) {

        if (jsonArray.isEmpty()) {
            return Text.literal("[]");
        }

        MutableText result = Text.literal("[");
        if (!indent.isEmpty()) {
            result.append("\n");
        }

        Iterator<JsonElement> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {

            JsonElement jsonElement = iterator.next();
            Optional<Text> jsonText = new JsonTextFormatter(indent, offset + 1, false).applyInternal(jsonElement);

            jsonText.ifPresent(text -> result
                .append(Strings.repeat(indent, offset))
                .append(text));

            if (iterator.hasNext() && jsonText.isPresent()) {
                result.append(!indent.isEmpty() ? ",\n" : ", ");
            }

        }

        if (!indent.isEmpty()) {
            result.append("\n");
        }

        if (!root) {
            result.append(Strings.repeat(indent, offset - 1));
        }

        return result.append("]");

    }

    public Text visitObject(JsonObject jsonObject) {

        if (jsonObject.isEmpty()) {
            return Text.literal("{}");
        }

        MutableText result = Text.literal("{");
        if (!indent.isEmpty()) {
            result.append("\n");
        }

        Iterator<Map.Entry<String, JsonElement>> iterator = jsonObject.entrySet().iterator();
        while (iterator.hasNext()) {

            Map.Entry<String, JsonElement> entry = iterator.next();

            Text name = Text.literal(entry.getKey()).formatted(NAME_COLOR);
            Optional<Text> jsonText = new JsonTextFormatter(indent, offset + 1, false).applyInternal(entry.getValue());

            jsonText.ifPresent(text -> result
                .append(Strings.repeat(indent, offset))
                .append(name).append(": ")
                .append(text));
            
            if (iterator.hasNext() && jsonText.isPresent()) {
                result.append(!indent.isEmpty() ? ",\n" : ", ");
            }

        }
        
        if (!indent.isEmpty()) {
            result.append("\n");
        }

        if (!root) {
            result.append(Strings.repeat(indent, offset - 1));
        }

        return result.append("}");

    }

    public Text visitPrimitive(JsonPrimitive jsonPrimitive) {

        if (jsonPrimitive.isBoolean()) {
            return Text.literal(String.valueOf(jsonPrimitive.getAsBoolean())).formatted(BOOLEAN_COLOR);
        }

        else if (jsonPrimitive.isString()) {
            return Text.literal("\"" + jsonPrimitive.getAsString() + "\"").formatted(STRING_COLOR);
        }

        else if (jsonPrimitive.isNumber()) {

            Number number = jsonPrimitive.getAsNumber();

            return switch (number) {
                case Integer i ->
                    Text.literal(i.toString()).formatted(NUMBER_COLOR);
                case Long l ->
                    Text.literal(l.toString()).formatted(NUMBER_COLOR)
                        .append(Text.literal("L").formatted(TYPE_SUFFIX_COLOR));
                case Float f ->
                    Text.literal(f.toString()).formatted(NUMBER_COLOR)
                        .append(Text.literal("F").formatted(TYPE_SUFFIX_COLOR));
                case Double d ->
                    Text.literal(d.toString()).formatted(NUMBER_COLOR)
                        .append(Text.literal("D").formatted(TYPE_SUFFIX_COLOR));
                case Byte b ->
                    Text.literal(b.toString()).formatted(NUMBER_COLOR)
                        .append(Text.literal("B")).formatted(TYPE_SUFFIX_COLOR);
                case Short s ->
                    Text.literal(s.toString()).formatted(NUMBER_COLOR)
                        .append(Text.literal("S")).formatted(TYPE_SUFFIX_COLOR);
                case null ->
                    throw new JsonSyntaxException("Number cannot be null!");
                default ->
                    throw new JsonParseException("The type of number " + number + " is not supported!");
            };

        }

        else {
            throw new JsonParseException("The format of JSON primitive " + jsonPrimitive + " is not supported!");
        }

    }

}
