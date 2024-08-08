package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.IdentifiedLootTable;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.util.SavedBlockPosition;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ReplaceLootTablePowerType extends PowerType {

    public static final RegistryKey<LootTable> REPLACED_TABLE_KEY = RegistryKey.of(RegistryKeys.LOOT_TABLE, Apoli.identifier("replaced_loot_table"));
    public static Identifier LAST_REPLACED_TABLE_ID;

    private static final Stack<LootTable> REPLACEMENT_STACK = new Stack<>();
    private static final Stack<LootTable> BACKTRACK_STACK = new Stack<>();

    private final Map<Pattern, Identifier> replacements;

    private final int priority;

    private final Predicate<Pair<World, ItemStack>> itemCondition;
    private final Predicate<Pair<Entity, Entity>> biEntityCondition;
    private final Predicate<CachedBlockPosition> blockCondition;

    public ReplaceLootTablePowerType(Power power, LivingEntity entity, Map<Pattern, Identifier> replacements, int priority, Predicate<Pair<World, ItemStack>> itemCondition, Predicate<Pair<Entity, Entity>> biEntityCondition, Predicate<CachedBlockPosition> blockCondition) {
        super(power, entity);
        this.replacements = replacements;
        this.priority = priority;
        this.itemCondition = itemCondition;
        this.biEntityCondition = biEntityCondition;
        this.blockCondition = blockCondition;
    }

    public boolean hasReplacement(RegistryKey<LootTable> lootTableKey) {

        String id = lootTableKey.getValue().toString();

        return replacements.keySet()
            .stream()
            .anyMatch(regex -> regex.pattern().equals(id) || regex.matcher(id).matches());

    }

    public boolean doesApply(LootContext context) {

        Entity contextEntity = context.get(LootContextParameters.THIS_ENTITY);
        ItemStack toolStack = context.hasParameter(LootContextParameters.TOOL) ? context.get(LootContextParameters.TOOL) : ItemStack.EMPTY;
        SavedBlockPosition savedBlockPosition = SavedBlockPosition.fromLootContext(context);

        return doesApply(contextEntity, toolStack, savedBlockPosition);

    }

    public boolean doesApply(Entity contextEntity, ItemStack toolStack, SavedBlockPosition cachedBlock) {
        return (biEntityCondition == null || biEntityCondition.test(new Pair<>(entity, contextEntity)))
            && (itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), toolStack)))
            && (blockCondition == null || blockCondition.test(cachedBlock));
    }

    @Nullable
    public RegistryKey<LootTable> getReplacement(RegistryKey<LootTable> lootTableKey) {
        String lootTableId = lootTableKey.getValue().toString();
        return replacements.entrySet()
            .stream()
            .filter(entry -> entry.getKey().pattern().equals(lootTableId) || entry.getKey().matcher(lootTableId).matches())
            .findFirst()
            .map(entry -> RegistryKey.of(RegistryKeys.LOOT_TABLE, entry.getValue()))
            .orElse(null);
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
            stringBuilder.append(t == null ? "null" : ((IdentifiedLootTable)t).apoli$getLootTableKey());
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
            stringBuilder.append(t == null ? "null" : ((IdentifiedLootTable)t).apoli$getLootTableKey());
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

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("replace_loot_table"),
            new SerializableData()
                .add("replace", ApoliDataTypes.REGEX_MAP)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new ReplaceLootTablePowerType(power, entity,
                data.get("replace"),
                data.get("priority"),
                data.get("item_condition"),
                data.get("bientity_condition"),
                data.get("block_condition")
            )
        ).allowCondition();
    }

}
