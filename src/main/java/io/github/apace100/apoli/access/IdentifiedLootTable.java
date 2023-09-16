package io.github.apace100.apoli.access;

import net.minecraft.loot.LootManager;
import net.minecraft.util.Identifier;

public interface IdentifiedLootTable {

    void apoli$setId(Identifier id, LootManager lootManager);

    Identifier apoli$getId();
}
