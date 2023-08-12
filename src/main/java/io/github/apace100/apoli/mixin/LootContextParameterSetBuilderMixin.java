package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ReplacingLootContextParameterSet;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootContextParameterSet.Builder.class)
public class LootContextParameterSetBuilderMixin {
    @Inject(method = "build", at = @At("RETURN"))
    private void setLootContextType(LootContextType type, CallbackInfoReturnable<LootContext> cir) {
        ReplacingLootContextParameterSet rlc = (ReplacingLootContextParameterSet) cir.getReturnValue();
        rlc.apoli$setType(type);
    }
}
