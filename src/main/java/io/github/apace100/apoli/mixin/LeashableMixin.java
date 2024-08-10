package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.apace100.apoli.access.CustomLeashable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.minecraft.item.ItemConvertible;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Leashable.class)
public interface LeashableMixin {

    @WrapWithCondition(method = "detachLeash(Lnet/minecraft/entity/Entity;ZZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;dropItem(Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/entity/ItemEntity;"))
    private static boolean apoli$preventDroppingLeashOnCustom(Entity entity, ItemConvertible item) {
        return !(entity instanceof CustomLeashable customLeashable)
            || !customLeashable.apoli$isCustomLeashed();
    }

    @Inject(method = "detachLeash(Lnet/minecraft/entity/Entity;ZZ)V", at = @At("TAIL"))
    private static void apoli$resetCustomLeashStatus(Entity entity, boolean sendPacket, boolean dropItem, CallbackInfo ci) {

        if (entity instanceof CustomLeashable customLeashable) {
            customLeashable.apoli$setCustomLeashed(false);
        }

    }

}
