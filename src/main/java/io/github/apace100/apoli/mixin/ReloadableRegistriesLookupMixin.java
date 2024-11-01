package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.KeyableLootTable;
import io.github.apace100.apoli.power.type.ReplaceLootTablePowerType;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ReloadableRegistries.Lookup.class)
public abstract class ReloadableRegistriesLookupMixin {

    @Shadow
    public abstract DynamicRegistryManager.Immutable getRegistryManager();

    @ModifyReturnValue(method = "getLootTable", at = @At("RETURN"))
    private LootTable apoli$replaceLootTableOnLookup(LootTable original, RegistryKey<LootTable> lootTableKey) {

        if (original instanceof KeyableLootTable keyableLootTable) {
            keyableLootTable.apoli$setup(lootTableKey, thisAsLookup());
        }

        if (lootTableKey.equals(ReplaceLootTablePowerType.REPLACED_TABLE_KEY)) {

            Registry<LootTable> lootTableRegistry = this.getRegistryManager().get(RegistryKeys.LOOT_TABLE);
            LootTable replacementTable = ReplaceLootTablePowerType.peek();

            Apoli.LOGGER.debug("Replacing loot table \"{}\" with \"{}\"...", lootTableRegistry.getId(original), lootTableRegistry.getId(replacementTable));
            return replacementTable;

        }

        return original;

    }

    @Unique
    private ReloadableRegistries.Lookup thisAsLookup() {
        return (ReloadableRegistries.Lookup) (Object) this;
    }

}
