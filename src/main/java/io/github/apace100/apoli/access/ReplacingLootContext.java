package io.github.apace100.apoli.access;

import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextType;

public interface ReplacingLootContext {

    void apoli$setType(LootContextType type);

    LootContextType apoli$getType();

    void apoli$setReplaced(LootTable table);

    boolean apoli$isReplaced(LootTable table);
}
