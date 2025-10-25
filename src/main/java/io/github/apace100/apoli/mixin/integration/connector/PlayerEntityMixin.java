package io.github.apace100.apoli.mixin.integration.connector;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyHarvestPower;
import io.github.apace100.apoli.util.HarvestContext;
import io.github.apace100.apoli.util.SavedBlockPosition;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "canHarvest", at = @At("HEAD"), cancellable = true)
    private void origins$modifyHarvestCheck(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        SavedBlockPosition saved = HarvestContext.getBlockPosition();

        PlayerEntity player = (PlayerEntity)(Object)this;

        for (ModifyHarvestPower power : PowerHolderComponent.getPowers(player, ModifyHarvestPower.class)) {
            if (power.doesApply(saved)) {
                cir.setReturnValue(power.isHarvestAllowed());
                return;
            }
        }
    }
}
