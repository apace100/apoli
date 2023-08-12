package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.HiddenEffectStatus;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StatusEffectInstance.class)
public class StatusEffectInstanceMixin implements HiddenEffectStatus {
    @Shadow
    @Nullable
    private StatusEffectInstance hiddenEffect;

    public @Nullable StatusEffectInstance apoli$getHiddenEffect() {
        return this.hiddenEffect;
    }
}
