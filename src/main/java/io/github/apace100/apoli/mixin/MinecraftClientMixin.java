package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.OverlaySpriteHolder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.EntityGlowPower;
import io.github.apace100.apoli.power.OverlayPower;
import io.github.apace100.apoli.power.SelfGlowPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements OverlaySpriteHolder {

    @Shadow public ClientPlayerEntity player;

    @Shadow @Final private TextureManager textureManager;

    @Shadow @Final private ReloadableResourceManagerImpl resourceManager;

    @ModifyReturnValue(method = "hasOutline", at = @At("RETURN"))
    private boolean apoli$makeEntitiesGlow(boolean original, Entity entity) {
        return original
            || (player != entity && PowerHolderComponent.hasPower(player, EntityGlowPower.class, p -> p.doesApply(entity)))
            || PowerHolderComponent.hasPower(entity, SelfGlowPower.class, p -> p.doesApply(player));
    }

    @Unique
    private OverlayPower.SpriteHolder apoli$overlaySpriteHolder;

    @Override
    public Sprite apoli$getSprite(Identifier id) {
        return apoli$overlaySpriteHolder.getSprite(id);
    }

    @Inject(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/render/item/ItemRenderer;)Lnet/minecraft/client/gui/hud/InGameHud;"))
    private void apoli$registerCustomAtlases(RunArgs args, CallbackInfo ci) {
        this.apoli$overlaySpriteHolder = new OverlayPower.SpriteHolder(this.textureManager);
        this.resourceManager.registerReloader(apoli$overlaySpriteHolder);
    }

}
