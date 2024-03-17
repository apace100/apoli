package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.IdentifiedLootTable;
import io.github.apace100.apoli.power.ReplaceLootTablePower;
import net.minecraft.loot.*;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(LootDataLookup.class)
public interface LootDataLookupMixin extends LootDataLookup {

    @Shadow <T> Optional<T> getElementOptional(LootDataKey<T> key);

    @ModifyReturnValue(method = "getLootTable", at = @At("RETURN"))
    private LootTable apoli$replaceLootTableOnLookUp(LootTable original, Identifier id) {

        if (id.equals(ReplaceLootTablePower.REPLACED_TABLE_UTIL_ID)) {

            LootTable replacement = ReplaceLootTablePower.peek();
            Apoli.LOGGER.info("Replacing \"{}\" with \"{}\"", id, ((IdentifiedLootTable) replacement).apoli$getId());

            return replacement;

        }

        this.getElementOptional(LootDataType.LOOT_TABLES, id).ifPresent(lootTable -> {

            if (lootTable instanceof IdentifiedLootTable identifiedLootTable) {
                identifiedLootTable.apoli$setId(id, (LootManager) this);
            }

        });

        return original;

    }

}
