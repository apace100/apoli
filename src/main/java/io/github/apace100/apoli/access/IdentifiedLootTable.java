package io.github.apace100.apoli.access;

import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.ReloadableRegistries;

public interface IdentifiedLootTable {

    void apoli$setKey(RegistryKey<LootTable> lootTableKey, ReloadableRegistries.Lookup registryLookup);

    RegistryKey<LootTable> apoli$getLootTableKey();

}
