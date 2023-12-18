package io.github.apace100.apoli.util;

import com.google.common.base.Strings;
import com.google.gson.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Iterator;
import java.util.Map;

public class JsonTextFormatter {

    private static final Formatting NULL_COLOR = Formatting.LIGHT_PURPLE;
    private static final Formatting NAME_COLOR = Formatting.AQUA;
    private static final Formatting STRING_COLOR = Formatting.GREEN;
    private static final Formatting NUMBER_COLOR = Formatting.GOLD;
    private static final Formatting BOOLEAN_COLOR = Formatting.BLUE;
    private static final Formatting TYPE_SUFFIX_COLOR = Formatting.RED;

    private final String indent;
    private final int indentOffset;

    private Text result;
    private boolean root;

    public JsonTextFormatter(String indent) {
        this(indent, 1);
    }

    protected JsonTextFormatter(String indent, int indentOffset) {
        this.indent = indent;
        this.indentOffset = Math.max(0, indentOffset);
        this.result = ScreenTexts.EMPTY;
        this.root = true;
    }

    public Text apply(JsonElement jsonElement) {

        if (!handleJsonElement(jsonElement)) {
            throw new JsonParseException("The format of the specified JSON element is not supported!");
        }

        return this.result;

    }

    protected Text apply(JsonElement jsonElement, boolean rootElement) {
        this.root = rootElement;
        return apply(jsonElement);
    }

    protected final boolean handleJsonElement(JsonElement jsonElement) {

        if (jsonElement instanceof JsonArray jsonArray) {
            visitArray(jsonArray);
            return true;
        }

        else if (jsonElement instanceof JsonObject jsonObject) {
            visitObject(jsonObject);
            return true;
        }

        else if (jsonElement instanceof JsonPrimitive jsonPrimitive) {
            visitPrimitive(jsonPrimitive);
            return true;
        }

        else if (jsonElement instanceof JsonNull) {
            this.result = Text.literal("null").formatted(NULL_COLOR);
            return true;
        }

        return false;

    }

    public void visitArray(JsonArray jsonArray) {

        if (jsonArray.isEmpty()) {
            this.result = Text.literal("[]");
            return;
        }

        MutableText result = Text.literal("[");
        if (!indent.isEmpty()) {
            result.append("\n");
        }

        Iterator<JsonElement> iterator = jsonArray.iterator();
        while (iterator.hasNext()) {

            JsonElement jsonElement = iterator.next();
            result
                .append(Strings.repeat(indent, indentOffset))
                .append(new JsonTextFormatter(indent, indentOffset + 1).apply(jsonElement, false));

            if (iterator.hasNext()) {
                result.append(!indent.isEmpty() ? ",\n" : ", ");
            }

        }

        if (!indent.isEmpty()) {
            result.append("\n");
        }

        if (!root) {
            result.append(Strings.repeat(indent, indentOffset - 1));
        }

        result.append("]");
        this.result = result;

    }

    public void visitObject(JsonObject jsonObject) {

        if (jsonObject.isEmpty()) {
            this.result = Text.literal("{}");
            return;
        }

        MutableText result = Text.literal("{");
        if (!indent.isEmpty()) {
            result.append("\n");
        }

        Iterator<Map.Entry<String, JsonElement>> iterator = jsonObject.entrySet().iterator();
        while (iterator.hasNext()) {

            Map.Entry<String, JsonElement> entry = iterator.next();
            Text name = Text.literal(entry.getKey()).formatted(NAME_COLOR);

            result
                .append(Strings.repeat(indent, indentOffset))
                .append(name).append(": ")
                .append(new JsonTextFormatter(indent, indentOffset + 1).apply(entry.getValue(), false));
            
            if (iterator.hasNext()) {
                result.append(!indent.isEmpty() ? ",\n" : ", ");
            }

        }
        
        if (!indent.isEmpty()) {
            result.append("\n");
        }

        if (!root) {
            result.append(Strings.repeat(indent, indentOffset - 1));
        }

        result.append("}");
        this.result = result;

    }

    public void visitPrimitive(JsonPrimitive jsonPrimitive) {

        if (!handlePrimitive(jsonPrimitive)) {
            throw new JsonParseException("Specified JSON primitive is not supported!");
        }

    }

    protected final boolean handlePrimitive(JsonPrimitive jsonPrimitive) {

        if (jsonPrimitive.isBoolean()) {
            this.result = Text.literal(String.valueOf(jsonPrimitive.getAsBoolean())).formatted(BOOLEAN_COLOR);
            return true;
        }

        else if (jsonPrimitive.isString()) {
            this.result = Text.literal("\"" + jsonPrimitive.getAsString() + "\"").formatted(STRING_COLOR);
            return true;
        }

        else if (jsonPrimitive.isNumber()) {

            Number number = jsonPrimitive.getAsNumber();
            Text numberText;

            if (number instanceof Integer i) {
                numberText = Text.literal(String.valueOf(i)).formatted(NUMBER_COLOR);
            }

            else if (number instanceof Long l) {
                numberText = Text.literal(String.valueOf(l)).formatted(NUMBER_COLOR)
                    .append(Text.literal("L").formatted(TYPE_SUFFIX_COLOR));
            }

            else if (number instanceof Float f) {
                numberText = Text.literal(String.valueOf(f)).formatted(NUMBER_COLOR)
                    .append(Text.literal("F").formatted(TYPE_SUFFIX_COLOR));
            }

            else if (number instanceof Double d) {
                numberText = Text.literal(String.valueOf(d)).formatted(NUMBER_COLOR)
                    .append(Text.literal("D").formatted(TYPE_SUFFIX_COLOR));
            }

            else if (number instanceof Byte b) {
                numberText = Text.literal(String.valueOf(b)).formatted(NUMBER_COLOR)
                    .append(Text.literal("B")).formatted(TYPE_SUFFIX_COLOR);
            }

            else if (number instanceof Short s) {
                numberText = Text.literal(String.valueOf(s)).formatted(NUMBER_COLOR)
                    .append(Text.literal("S")).formatted(TYPE_SUFFIX_COLOR);
            }

            else {
                return false;
            }

            this.result = numberText;
            return true;

        }

        return false;

    }

}
