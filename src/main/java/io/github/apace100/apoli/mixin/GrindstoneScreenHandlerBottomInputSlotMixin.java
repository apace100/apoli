package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyGrindstonePower;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$3")
public class GrindstoneScreenHandlerBottomInputSlotMixin {

    @Final
    @Shadow
    GrindstoneScreenHandler field_16778;

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void allowPowerStacks(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        PowerModifiedGrindstone pmg = (PowerModifiedGrindstone) field_16778;
        if(PowerHolderComponent.hasPower(pmg.apoli$getPlayer(), ModifyGrindstonePower.class, p -> p.allowsInBottom(stack))) {
            cir.setReturnValue(true);
        }
    }
}
