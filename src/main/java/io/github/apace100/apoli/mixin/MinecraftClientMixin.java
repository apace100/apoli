package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.EntityGlowPower;
import io.github.apace100.apoli.power.SelfGlowPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow public ClientPlayerEntity player;

    @ModifyReturnValue(method = "hasOutline", at = @At("RETURN"))
    private boolean apoli$makeEntitiesGlow(boolean original, Entity entity) {
        return original
            || (player != entity && PowerHolderComponent.hasPower(player, EntityGlowPower.class, p -> p.doesApply(entity)))
            || PowerHolderComponent.hasPower(entity, SelfGlowPower.class, p -> p.doesApply(player));
    }

}
