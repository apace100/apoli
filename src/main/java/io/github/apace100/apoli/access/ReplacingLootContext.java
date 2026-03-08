package io.github.apace100.apoli.access;

import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;

public interface ReplacingLootContext extends LootContextTypeHolder {

    void apoli$setReplaced(RegistryKey<LootTable> key);

    boolean apoli$isReplaced(RegistryKey<LootTable> key);
}
