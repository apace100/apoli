package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.IdentifiedLootTable;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ReplaceLootTablePower extends Power {

    public static final Identifier REPLACED_TABLE_UTIL_ID = new Identifier(Apoli.MODID, "replaced_loot_table");
    public static Identifier LAST_REPLACED_TABLE_ID;

    private static final Stack<LootTable> REPLACEMENT_STACK = new Stack<>();
    private static final Stack<LootTable> BACKTRACK_STACK = new Stack<>();

    private final Map<Pattern, Identifier> replacements;

    private final int priority;

    private final Predicate<Pair<World, ItemStack>> itemCondition;
    private final Predicate<Pair<Entity, Entity>> biEntityCondition;
    private final Predicate<CachedBlockPosition> blockCondition;

    public ReplaceLootTablePower(PowerType<?> type, LivingEntity entity, Map<Pattern, Identifier> replacements, int priority, Predicate<Pair<World, ItemStack>> itemCondition, Predicate<Pair<Entity, Entity>> biEntityCondition, Predicate<CachedBlockPosition> blockCondition) {
        super(type, entity);
        this.replacements = replacements;
        this.priority = priority;
        this.itemCondition = itemCondition;
        this.biEntityCondition = biEntityCondition;
        this.blockCondition = blockCondition;
    }

    public boolean hasReplacement(Identifier id) {

        String idString = id.toString();

        return replacements.keySet()
            .stream()
            .anyMatch(regex -> regex.pattern().equals(idString) || regex.matcher(idString).matches());

    }

    public boolean doesApply(LootContext lootContext) {

        if (biEntityCondition != null && !biEntityCondition.test(new Pair<>(entity, lootContext.get(LootContextParameters.THIS_ENTITY)))) {
            return false;
        }

        if (itemCondition != null && (lootContext.hasParameter(LootContextParameters.TOOL) && !itemCondition.test(new Pair<>(entity.getWorld(), lootContext.get(LootContextParameters.TOOL))))) {
            return false;
        }

        Vec3d originPos = lootContext.get(LootContextParameters.ORIGIN);
        if (blockCondition != null && originPos != null) {

            BlockPos blockPos = BlockPos.ofFloored(originPos);
            CachedBlockPosition cachedBlockPosition = new CachedBlockPosition(lootContext.getWorld(), blockPos, true);

            return blockCondition.test(cachedBlockPosition);

        }

        return true;

    }

    @Nullable
    public Identifier getReplacement(Identifier id) {

        String idString = id.toString();
        for (Pattern regex : replacements.keySet()) {

            if (regex.pattern().equals(idString) || regex.matcher(idString).matches()) {
                return replacements.get(regex);
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
            stringBuilder.append(t == null ? "null" : ((IdentifiedLootTable)t).apoli$getId());
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
            stringBuilder.append(t == null ? "null" : ((IdentifiedLootTable)t).apoli$getId());
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
            Apoli.identifier("replace_loot_table"),
            new SerializableData()
                .add("replace", ApoliDataTypes.REGEX_MAP)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (powerType, livingEntity) -> new ReplaceLootTablePower(
                powerType,
                livingEntity,
                data.get("replace"),
                data.get("priority"),
                data.get("item_condition"),
                data.get("bientity_condition"),
                data.get("block_condition")
            )
        ).allowCondition();
    }

}
