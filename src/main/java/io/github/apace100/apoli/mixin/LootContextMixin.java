package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ReplacingLootContext;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Set;

@Mixin(LootContext.class)
public class LootContextMixin implements ReplacingLootContext {

    @Unique
    private LootContextType apoli$lootContextType;

    @Unique
    private final Set<LootTable> apoli$replacedTables = new HashSet<>();

    @Override
    public void apoli$setType(LootContextType type) {
        apoli$lootContextType = type;
    }

    @Override
    public LootContextType apoli$getType() {
        return apoli$lootContextType;
    }

    @Override
    public void apoli$setReplaced(LootTable table) {
        apoli$replacedTables.add(table);
    }

    @Override
    public boolean apoli$isReplaced(LootTable table) {
        return apoli$replacedTables.contains(table);
    }
}
