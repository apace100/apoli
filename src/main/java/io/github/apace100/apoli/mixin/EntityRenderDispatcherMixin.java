package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PreventEntityRenderPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @ModifyReturnValue(method = "shouldRender", at = @At("RETURN"))
    private boolean apoli$preventRenderingEntities(boolean original, Entity entity) {
        return original
            && !PowerHolderComponent.hasPower(MinecraftClient.getInstance().player, PreventEntityRenderPower.class, p -> p.doesApply(entity));
    }

}
