package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.apace100.apoli.screen.toast.PositionAwareToast;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ToastManager.Entry.class)
public abstract class ToastManagerEntryMixin {

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
    private void apoli$test(MatrixStack matrices, float x, float y, float z, Operation<Void> original, @Share("toastX") LocalIntRef sharedToastX, @Share("toastY") LocalIntRef sharedToastY) {

        sharedToastX.set((int) x);
        sharedToastY.set((int) y);

        original.call(matrices, x, y, z);

    }

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/toast/Toast;draw(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/toast/ToastManager;J)Lnet/minecraft/client/toast/Toast$Visibility;"))
    private Toast.Visibility apoli$drawAsPositionAwareToast(Toast toast, DrawContext context, ToastManager toastManager, long startTime, Operation<Toast.Visibility> original, @Share("toastX") LocalIntRef sharedToastX, @Share("toastY") LocalIntRef sharedToastY) {
        return toast instanceof PositionAwareToast positionAwareToast
            ? positionAwareToast.draw(sharedToastX.get(), sharedToastY.get(), context, toastManager, startTime)
            : original.call(toast, context, toastManager, startTime);
    }

}
