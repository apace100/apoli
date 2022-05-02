package io.github.apace100.apoli.access;

import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextType;

public interface ReplacingLootContext {

    void setType(LootContextType type);

    LootContextType getType();

    void setReplaced(LootTable table);

    boolean isReplaced(LootTable table);
}
