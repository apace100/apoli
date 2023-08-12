package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.IdentifiedLootTable;
import io.github.apace100.apoli.power.ReplaceLootTablePower;
import net.minecraft.loot.LootDataLookup;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LootDataLookup.class)
public interface LootDataLookupMixin extends LootDataLookup
{
    @Inject(method = "getLootTable", at = @At("HEAD"), cancellable = true)
    private void setTableId(Identifier id, CallbackInfoReturnable<LootTable> cir) {
        if(id.equals(ReplaceLootTablePower.REPLACED_TABLE_UTIL_ID)) {
            LootTable replace = ReplaceLootTablePower.peek();
            Apoli.LOGGER.info("Replacing " + id + " with " + ((IdentifiedLootTable)replace).apoli$getId());
            cir.setReturnValue(replace);
            //cir.setReturnValue(getTable(ReplaceLootTablePower.LAST_REPLACED_TABLE_ID));
        } else {
            Optional<LootTable> tableOptional = this.getElementOptional(LootDataType.LOOT_TABLES, id);
            if(tableOptional.isPresent()) {
                LootTable table = tableOptional.get();
                if(table instanceof IdentifiedLootTable identifiedLootTable) {
                    identifiedLootTable.apoli$setId(id, (LootManager)(Object)this);
                }
            }
        }
    }
}
