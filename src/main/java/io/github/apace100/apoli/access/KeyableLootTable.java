package io.github.apace100.apoli.access;

import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.ReloadableRegistries;

public interface KeyableLootTable {

    RegistryKey<LootTable> apoli$getKey();

    void apoli$setup(RegistryKey<LootTable> lootTableKey, ReloadableRegistries.Lookup lookup);

}
