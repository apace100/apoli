package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyGrindstonePowerType;
import io.github.apace100.apoli.util.InventoryUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin extends ScreenHandler implements PowerModifiedGrindstone {

    @Shadow
    @Final
    Inventory input;

    @Shadow
    @Final
    private Inventory result;

    @Shadow
    @Final
    public static int INPUT_1_ID;

    @Shadow
    @Final
    public static int INPUT_2_ID;

    @Unique
    private PlayerEntity apoli$cachedPlayer;

    @Unique
    private BlockPos apoli$cachedPosition;

    @Unique
    private List<ModifyGrindstonePowerType> apoli$appliedPowers;

    private GrindstoneScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("RETURN"))
    private void cachePlayer(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        apoli$cachedPlayer = playerInventory.player;
        apoli$cachedPosition = context.get((w, bp) -> bp).orElse(null);
    }

    @Inject(method = "updateResult", at = @At("RETURN"))
    private void modifyResult(CallbackInfo ci) {

        ItemStack topStack = input.getStack(INPUT_1_ID);
        ItemStack bottomStack = input.getStack(INPUT_2_ID);

        StackReference outputStackRef = InventoryUtil.createStackReference(result.getStack(0));
        this.apoli$appliedPowers = PowerHolderComponent.getPowerTypes(apoli$cachedPlayer, ModifyGrindstonePowerType.class)
            .stream()
            .filter(mgp -> mgp.doesApply(topStack, bottomStack, outputStackRef.get(), apoli$cachedPosition))
            .peek(mgp -> mgp.setOutput(topStack, bottomStack, outputStackRef))
            .toList();

        result.setStack(0, outputStackRef.get());
        this.sendContentUpdates();

    }

    @ModifyVariable(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"), ordinal = 1)
    private ItemStack performAfterGrindstoneActionsQuickMove(ItemStack original, PlayerEntity player, int slot) {

        StackReference newStackRef = InventoryUtil.createStackReference(original);
        List<ModifyGrindstonePowerType> applyingPowers = this.apoli$getAppliedPowers();

        if (slot != 2 || applyingPowers == null) {
            return original;
        }

        for (ModifyGrindstonePowerType applyingPower : applyingPowers) {
            applyingPower.applyAfterGrindingItemAction(newStackRef);
            applyingPower.executeActions(this.apoli$getPos());
        }

        return newStackRef.get();

    }

    @Override
    public List<ModifyGrindstonePowerType> apoli$getAppliedPowers() {
        return apoli$appliedPowers;
    }

    @Override
    public PlayerEntity apoli$getPlayer() {
        return apoli$cachedPlayer;
    }

    @Nullable
    @Override
    public BlockPos apoli$getPos() {
        return apoli$cachedPosition;
    }

}
