package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ReplacingLootContextParameterSet;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LootContextParameterSet.class)
public class LootContextParameterSetMixin implements ReplacingLootContextParameterSet {

    @Unique
    private LootContextType apoli$lootContextType;

    @Override
    public void apoli$setType(LootContextType type) {
        apoli$lootContextType = type;
    }

    @Override
    public LootContextType apoli$getType() {
        return apoli$lootContextType;
    }

}
