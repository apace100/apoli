package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.ReplacingLootContext;
import io.github.apace100.apoli.access.ReplacingLootContextParameterSet;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootContext.Builder.class)
public class LootContextBuilderMixin {
    @Shadow
    @Final
    private LootContextParameterSet parameters;

    @Inject(method = "build", at = @At("RETURN"))
    private void setLootContextType(CallbackInfoReturnable<LootContext> cir) {
        ReplacingLootContextParameterSet rlcps = (ReplacingLootContextParameterSet) parameters;

        ReplacingLootContext rlc = (ReplacingLootContext) cir.getReturnValue();
        rlc.apoli$setType(rlcps.apoli$getType());
    }
}
