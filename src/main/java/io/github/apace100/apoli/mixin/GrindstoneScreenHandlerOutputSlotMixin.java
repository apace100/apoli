package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.power.ModifyGrindstonePower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$4")
public class GrindstoneScreenHandlerOutputSlotMixin {

    @Final
    @Shadow
    GrindstoneScreenHandler field_16780;

    @Inject(method = "onTakeItem", at = @At(value = "INVOKE",target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 0))
    private void executeGrindstoneActions(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        PowerModifiedGrindstone pmg = (PowerModifiedGrindstone) field_16780;
        List<ModifyGrindstonePower> applyingPowers = pmg.apoli$getAppliedPowers();
        applyingPowers.forEach(mgp -> {
            mgp.applyAfterGrindingItemAction(stack);
            mgp.executeActions(pmg.apoli$getPos());
        });
    }

    @Inject(method = "getExperience(Lnet/minecraft/world/World;)I", at = @At("RETURN"), cancellable = true)
    private void modifyExperience(World world, CallbackInfoReturnable<Integer> cir) {
        PowerModifiedGrindstone pmg = (PowerModifiedGrindstone) field_16780;
        if(pmg.apoli$getAppliedPowers().size() == 0) {
            return;
        }
        List<Modifier> modifiers = pmg.apoli$getAppliedPowers().stream().map(ModifyGrindstonePower::getExperienceModifier).filter(Objects::nonNull).toList();
        if(modifiers.size() == 0) {
            return;
        }
        int xp = (int)ModifierUtil.applyModifiers(pmg.apoli$getPlayer(), modifiers, cir.getReturnValue());
        cir.setReturnValue(xp);
    }
}
