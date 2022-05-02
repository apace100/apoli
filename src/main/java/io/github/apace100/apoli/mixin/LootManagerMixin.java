package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.IdentifiedLootTable;
import io.github.apace100.apoli.power.ReplaceLootTablePower;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LootManager.class)
public abstract class LootManagerMixin {

    @Shadow private Map<Identifier, LootTable> tables;

    @Shadow public abstract LootTable getTable(Identifier id);

    @Inject(method = "getTable", at = @At("HEAD"), cancellable = true)
    private void setTableId(Identifier id, CallbackInfoReturnable<LootTable> cir) {
        if(id.equals(ReplaceLootTablePower.REPLACED_TABLE_UTIL_ID)) {
            LootTable replace = ReplaceLootTablePower.peek();
            Apoli.LOGGER.info("Replacing " + id + " with " + ((IdentifiedLootTable)replace).getId());
            cir.setReturnValue(replace);
            //cir.setReturnValue(getTable(ReplaceLootTablePower.LAST_REPLACED_TABLE_ID));
        } else
        if(this.tables.containsKey(id)) {
            LootTable table = this.tables.get(id);
            if(table instanceof IdentifiedLootTable identifiedLootTable) {
                identifiedLootTable.setId(id, (LootManager)(Object)this);
            }
        }
    }
}
