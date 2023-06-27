package io.github.apace100.apoli.access;

import net.minecraft.loot.context.LootContextType;

public interface ReplacingLootContextParameterSet {

    void setType(LootContextType type);

    LootContextType getType();

}
