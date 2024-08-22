package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
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
import net.minecraft.screen.slot.Slot;
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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Shadow
    @Final
    public static int OUTPUT_ID;

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
        apoli$cachedPosition = context.get((world, pos) -> pos).orElse(null);
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
            .collect(Collectors.toCollection(LinkedList::new));

        result.setStack(0, outputStackRef.get());
        this.sendContentUpdates();

    }

    @ModifyVariable(method = "quickMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"), ordinal = 1)
    private ItemStack performAfterGrindstoneActionsQuickMove(ItemStack original, PlayerEntity player, int slotIndex, @Local Slot slot) {

        List<ModifyGrindstonePowerType> applyingPowers = this.apoli$getAppliedPowers();
        StackReference stackReference = InventoryUtil.createStackReference(original);

        if (slotIndex != OUTPUT_ID || applyingPowers == null || applyingPowers.isEmpty()) {
            return original;
        }

        ItemStack copy = original.copy();
        applyingPowers.forEach(mgpt -> mgpt.executeActions(this.apoli$getPos(), stackReference));

        if (stackReference.get().isEmpty()) {
            this.getSlot(slotIndex).onTakeItem(player, copy);
        }

        return stackReference.get();

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
