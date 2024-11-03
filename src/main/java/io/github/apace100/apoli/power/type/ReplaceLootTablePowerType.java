package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.KeyableLootTable;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.SavedBlockPosition;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Pattern;

public class ReplaceLootTablePowerType extends PowerType implements Prioritized<ReplaceLootTablePowerType> {

    public static final RegistryKey<LootTable> REPLACED_TABLE_KEY = RegistryKey.of(RegistryKeys.LOOT_TABLE, Apoli.identifier("replaced_loot_table"));
    public static Identifier LAST_REPLACED_TABLE_ID;

    private static final Stack<LootTable> REPLACEMENT_STACK = new Stack<>();
    private static final Stack<LootTable> BACKTRACK_STACK = new Stack<>();

    public static final TypedDataObjectFactory<ReplaceLootTablePowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("replace", ApoliDataTypes.REGEX_MAP, null)
            .addFunctionedDefault("replacements", ApoliDataTypes.REGEX_MAP, data -> data.get("replace"))
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("priority", SerializableDataTypes.INT, 0)
            .validate(MiscUtil.validateAnyFieldsPresent("replace", "replacements")),
        (data, condition) -> new ReplaceLootTablePowerType(
            data.get("replacements"),
            data.get("bientity_condition"),
            data.get("block_condition"),
            data.get("item_condition"),
            data.get("priority"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("replacements", powerType.replacements)
            .set("bientity_condition", powerType.biEntityCondition)
            .set("block_condition", powerType.blockCondition)
            .set("item_condition", powerType.itemCondition)
            .set("priority", powerType.getPriority())
    );

    private final Map<Pattern, Identifier> replacements;
    private final Optional<BiEntityCondition> biEntityCondition;

    private final Optional<BlockCondition> blockCondition;
    private final Optional<ItemCondition> itemCondition;

    private final int priority;

    public ReplaceLootTablePowerType(Map<Pattern, Identifier> replacements, Optional<BiEntityCondition> biEntityCondition, Optional<BlockCondition> blockCondition, Optional<ItemCondition> itemCondition, int priority, Optional<EntityCondition> condition) {
        super(condition);
        this.replacements = replacements;
        this.biEntityCondition = biEntityCondition;
        this.blockCondition = blockCondition;
        this.itemCondition = itemCondition;
        this.priority = priority;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.REPLACE_LOOT_TABLE;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean hasReplacement(RegistryKey<LootTable> lootTableKey) {

        Identifier id = lootTableKey.getValue();
        String idString = id.toString();

        return replacements.keySet()
            .stream()
            .anyMatch(regex -> regex.pattern().equals(idString) || regex.matcher(idString).matches());

    }

    public boolean doesApply(LootContext context) {

        Entity contextEntity = context.get(LootContextParameters.THIS_ENTITY);
        ItemStack toolStack = context.hasParameter(LootContextParameters.TOOL) ? context.get(LootContextParameters.TOOL) : ItemStack.EMPTY;

        return doesApply(contextEntity, toolStack, SavedBlockPosition.fromLootContext(context));

    }

    public boolean doesApply(Entity contextEntity, ItemStack toolStack, SavedBlockPosition savedBlock) {
        return itemCondition.map(condition -> condition.test(getHolder().getWorld(), toolStack)).orElse(true)
            && blockCondition.map(condition -> condition.test(savedBlock)).orElse(true)
            && biEntityCondition.map(condition -> condition.test(getHolder(), contextEntity)).orElse(true);
    }

    public Optional<RegistryKey<LootTable>> getReplacement(RegistryKey<LootTable> lootTableKey) {
        String lootTableId = lootTableKey.getValue().toString();
        return replacements.entrySet()
            .stream()
            .filter(entry -> entry.getKey().pattern().equals(lootTableId) || entry.getKey().matcher(lootTableId).matches())
            .map(Map.Entry::getValue)
            .findFirst()
            .map(replacementId -> RegistryKey.of(RegistryKeys.LOOT_TABLE, replacementId));
    }

    public static void clearStack() {
        REPLACEMENT_STACK.clear();
        BACKTRACK_STACK.clear();
    }

    public static void addToStack(LootTable lootTable) {
        REPLACEMENT_STACK.add(lootTable);
    }

    public static LootTable pop() {

        if (REPLACEMENT_STACK.isEmpty()) {
            return LootTable.EMPTY;
        }

        LootTable table = REPLACEMENT_STACK.pop();
        BACKTRACK_STACK.push(table);

        return table;

    }

    public static LootTable restore() {

        if (BACKTRACK_STACK.isEmpty()) {
            return LootTable.EMPTY;
        }

        LootTable table = BACKTRACK_STACK.pop();
        REPLACEMENT_STACK.push(table);

        return table;

    }

    public static LootTable peek() {

        if (REPLACEMENT_STACK.isEmpty()) {
            return LootTable.EMPTY;
        }

        else {
            return REPLACEMENT_STACK.peek();
        }

    }

    private static void printStacks() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        int count = 0;
        while(!REPLACEMENT_STACK.isEmpty()) {
            LootTable t = pop();
            stringBuilder.append(t == null ? "null" : ((KeyableLootTable)t).apoli$getKey());
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
        while(!BACKTRACK_STACK.isEmpty()) {
            LootTable t = restore();
            stringBuilder.append(t == null ? "null" : ((KeyableLootTable)t).apoli$getKey());
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

}
