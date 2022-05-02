package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ReplacingLootContext;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootContext.Builder.class)
public class LootContextBuilderMixin {

    @Inject(method = "build", at = @At("RETURN"))
    private void setLootContextType(LootContextType type, CallbackInfoReturnable<LootContext> cir) {
        ReplacingLootContext rlc = (ReplacingLootContext) cir.getReturnValue();
        rlc.setType(type);
    }
}
