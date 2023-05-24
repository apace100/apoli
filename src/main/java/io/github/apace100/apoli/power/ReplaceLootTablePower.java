package io.github.apace100.apoli.power;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.IdentifiedLootTable;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;

public class ReplaceLootTablePower extends Power {

    public static final Identifier REPLACED_TABLE_UTIL_ID = new Identifier(Apoli.MODID, "replaced_loot_table");
    public static Identifier LAST_REPLACED_TABLE_ID;

    private static Stack<LootTable> REPLACEMENT_STACK = new Stack<>();
    private static Stack<LootTable> BACKTRACK_STACK = new Stack<>();

    private final Map<String, Identifier> replacements;

    private final int priority;

    private final Predicate<ItemStack> itemCondition;
    private final Predicate<Pair<Entity, Entity>> biEntityCondition;
    private final Predicate<CachedBlockPosition> blockCondition;

    public ReplaceLootTablePower(PowerType<?> type, LivingEntity entity, Map<String, Identifier> replacements, int priority, Predicate<ItemStack> itemCondition, Predicate<Pair<Entity, Entity>> biEntityCondition, Predicate<CachedBlockPosition> blockCondition) {
        super(type, entity);
        this.replacements = replacements;
        this.priority = priority;
        this.itemCondition = itemCondition;
        this.biEntityCondition = biEntityCondition;
        this.blockCondition = blockCondition;
    }

    public boolean hasReplacement(Identifier id) {
        String idString = id.toString();
        if(replacements.containsKey(idString)) {
            return true;
        }
        return replacements.keySet().stream().anyMatch(idString::matches);
    }

    public boolean doesApply(LootContext lootContext) {
        if(biEntityCondition != null
            && !biEntityCondition.test(new Pair<>(entity, lootContext.get(LootContextParameters.THIS_ENTITY)))) {
            return false;
        }
        if(itemCondition != null
            && lootContext.hasParameter(LootContextParameters.TOOL)
            && !itemCondition.test(lootContext.get(LootContextParameters.TOOL))) {
            return false;
        }
        if(blockCondition != null && lootContext.hasParameter(LootContextParameters.ORIGIN)) {
            BlockPos blockPos = BlockPos.ofFloored(lootContext.get(LootContextParameters.ORIGIN));
            CachedBlockPosition cbp = new CachedBlockPosition(lootContext.getWorld(), blockPos, true);
            if(!blockCondition.test(cbp)) {
                return false;
            }
        }
        return true;
    }

    public Identifier getReplacement(Identifier id) {
        String idString = id.toString();
        if(replacements.containsKey(idString)) {
            return replacements.get(idString);
        }
        Set<String> keys = replacements.keySet();
        for(String s : keys) {
            if(idString.matches(s)) {
                return replacements.get(s);
            }
        }
        return null;
    }

    public int getPriority() {
        return priority;
    }

    public static void clearStack() {
        REPLACEMENT_STACK.clear();
        BACKTRACK_STACK.clear();
    }

    public static void addToStack(LootTable lootTable) {
        REPLACEMENT_STACK.add(lootTable);
    }

    public static LootTable pop() {
        if(REPLACEMENT_STACK.isEmpty()) {
            return LootTable.EMPTY;
        }
        LootTable table = REPLACEMENT_STACK.pop();
        BACKTRACK_STACK.push(table);
        return table;
    }

    public static LootTable restore() {
        if(BACKTRACK_STACK.isEmpty()) {
            return LootTable.EMPTY;
        }
        LootTable table = BACKTRACK_STACK.pop();
        REPLACEMENT_STACK.push(table);
        return table;
    }

    public static LootTable peek() {
        if(REPLACEMENT_STACK.isEmpty()) {
            return LootTable.EMPTY;
        }
        return REPLACEMENT_STACK.peek();
    }

    private static void printStacks() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        int count = 0;
        while(!REPLACEMENT_STACK.isEmpty()) {
            LootTable t = pop();
            stringBuilder.append(t == null ? "null" : ((IdentifiedLootTable)t).getId());
            if(!REPLACEMENT_STACK.isEmpty()) {
                stringBuilder.append(", ");
            }
            count++;
        }
        stringBuilder.append("], [");
        while(count > 0) {
            restore();
            count--;
        }
        while(BACKTRACK_STACK.size() > 0) {
            LootTable t = restore();
            stringBuilder.append(t == null ? "null" : ((IdentifiedLootTable)t).getId());
            if(!BACKTRACK_STACK.isEmpty()) {
                stringBuilder.append(", ");
            }
            count++;
        }
        while(count > 0) {
            pop();
            count--;
        }
        stringBuilder.append("]");
        Apoli.LOGGER.info(stringBuilder.toString());
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            new Identifier(Apoli.MODID, "replace_loot_table"),
            new SerializableData()
                .add("replace", REPLACEMENTS_DATA_TYPE)
                .add("priority", SerializableDataTypes.INT, 0)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
            data -> (type, player) -> new ReplaceLootTablePower(type, player,
                data.get("replace"),
                data.get("priority"), data.get("item_condition"),
                data.get("bientity_condition"),
                data.get("block_condition")))
            .allowCondition();
    }

    private static final SerializableDataType<Map<String, Identifier>> REPLACEMENTS_DATA_TYPE = new SerializableDataType<>(ClassUtil.castClass(Map.class),
        (packetByteBuf, stringIdentifierMap) -> {
            packetByteBuf.writeInt(stringIdentifierMap.size());
            stringIdentifierMap.forEach(((s, identifier) -> {
                packetByteBuf.writeString(s);
                packetByteBuf.writeIdentifier(identifier);
            }));
        },
        packetByteBuf -> {
            int count = packetByteBuf.readInt();
            Map<String, Identifier> map = new LinkedHashMap<>();
            for(int i = 0;i < count; i++) {
                String s = packetByteBuf.readString();
                Identifier id = packetByteBuf.readIdentifier();
                map.put(s, id);
            }
            return map;
        }, jsonElement -> {
            if(jsonElement.isJsonObject()) {
                JsonObject jo = jsonElement.getAsJsonObject();
                Map<String, Identifier> map = new LinkedHashMap<>();
                for(String s : jo.keySet()) {
                    JsonElement ele = jo.get(s);
                    if(!ele.isJsonPrimitive()) {
                        continue;
                    }
                    JsonPrimitive jp = ele.getAsJsonPrimitive();
                    if(!jp.isString()) {
                        continue;
                    }
                    Identifier id = new Identifier(jp.getAsString());
                    map.put(s, id);
                }
                return map;
            }
            throw new JsonParseException("Expected a JSON object");
        });
}
