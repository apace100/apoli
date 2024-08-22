package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.power.type.ModifyGrindstonePowerType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$2")
public class GrindstoneScreenHandlerTopInputSlotMixin {

    @Unique
    private GrindstoneScreenHandler apoli$grindstoneHandler;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void apoli$cacheGrindstone(GrindstoneScreenHandler grindstoneScreenHandler, Inventory inventory, int i, int j, int k, CallbackInfo ci) {
        this.apoli$grindstoneHandler = grindstoneScreenHandler;
    }

    @ModifyReturnValue(method = "canInsert", at = @At("RETURN"))
    private boolean apoli$allowStackInTopSlotViaPower(boolean original, ItemStack stack) {
        return original
            || ModifyGrindstonePowerType.allowsInTopSlot(apoli$grindstoneHandler, stack);
    }

}
