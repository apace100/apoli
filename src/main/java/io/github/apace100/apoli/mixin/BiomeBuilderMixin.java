package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.BiomeWeatherAccess;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.Builder.class)
public class BiomeBuilderMixin {

    @Shadow private @Nullable Float downfall;

    @Inject(method = "build", at = @At("RETURN"))
    private void apoli$storeDownfall(CallbackInfoReturnable<Biome> cir) {
        ((BiomeWeatherAccess)(Object)cir.getReturnValue()).setDownfall(downfall.floatValue());
    }
}
