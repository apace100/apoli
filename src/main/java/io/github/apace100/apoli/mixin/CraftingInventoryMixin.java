package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.PowerCraftingInventory;
import io.github.apace100.apoli.power.Power;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingInventory.class)
public abstract class CraftingInventoryMixin implements PowerCraftingInventory {

    @Unique
    private Power apoli$CachedPower;

    @Unique
    private PlayerEntity apoli$cachedPlayer;

    @Override
    public void apoli$setPower(Power power) {
        apoli$CachedPower = power;
    }

    @Override
    public Power apoli$getPower() {
        return apoli$CachedPower;
    }

    @Override
    public PlayerEntity apoli$getPlayer() {
        return apoli$cachedPlayer;
    }

    @Override
    public void apoli$setPlayer(PlayerEntity player) {
        this.apoli$cachedPlayer = player;
    }

    @Inject(method = "<init>(Lnet/minecraft/screen/ScreenHandler;IILnet/minecraft/util/collection/DefaultedList;)V", at = @At("TAIL"))
    private void apoli$cachePlayer(ScreenHandler handler, int width, int height, DefaultedList<ItemStack> stacks, CallbackInfo ci) {

        PlayerEntity player = switch (handler) {
            case CraftingScreenHandler craftingTable ->
                ((CraftingScreenHandlerAccessor) craftingTable).getPlayer();
            case PlayerScreenHandler playerInventory ->
                ((PlayerScreenHandlerAccessor) playerInventory).getOwner();
            default ->
                null;
        };

        this.apoli$setPlayer(player);

    }

}
