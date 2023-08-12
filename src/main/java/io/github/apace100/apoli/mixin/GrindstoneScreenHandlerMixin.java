package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyGrindstonePower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin extends ScreenHandler implements PowerModifiedGrindstone {

    @Shadow
    @Final
    private Inventory input;

    @Shadow @Final private Inventory result;

    @Unique
    private PlayerEntity apoli$cachedPlayer;

    @Unique
    private Optional<BlockPos> apoli$cachedPosition;

    @Unique
    private List<ModifyGrindstonePower> apoli$appliedPowers;

    protected GrindstoneScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("RETURN"))
    private void cachePlayer(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        apoli$cachedPlayer = playerInventory.player;
        apoli$cachedPosition = context.get((w, bp) -> bp);
    }

    @Inject(method = "updateResult", at = @At("RETURN"))
    private void modifyResult(CallbackInfo ci) {
        ItemStack top = input.getStack(0);
        ItemStack bottom = input.getStack(1);
        ItemStack output = result.getStack(0);
        List<ModifyGrindstonePower> applyingPowers = PowerHolderComponent.getPowers(apoli$cachedPlayer, ModifyGrindstonePower.class);
        applyingPowers = applyingPowers.stream().filter(mgp -> mgp.doesApply(top, bottom, output, apoli$cachedPosition)).toList();
        ItemStack newOutput = output;
        for(ModifyGrindstonePower mgp : applyingPowers) {
            newOutput = mgp.getOutput(top, bottom, newOutput);
        }
        apoli$appliedPowers = applyingPowers;
        result.setStack(0, newOutput);
        this.sendContentUpdates();
    }

    @Override
    public List<ModifyGrindstonePower> apoli$getAppliedPowers() {
        return apoli$appliedPowers;
    }

    @Override
    public PlayerEntity apoli$getPlayer() {
        return apoli$cachedPlayer;
    }

    @Override
    public Optional<BlockPos> apoli$getPos() {
        return apoli$cachedPosition;
    }
}
