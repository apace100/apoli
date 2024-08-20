package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.power.type.ActionOnItemPickupPowerType;
import io.github.apace100.apoli.power.type.PreventItemPickupPowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapOperation(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean apoli$onItemPickup(PlayerInventory instance, ItemStack stack, Operation<Boolean> original, PlayerEntity player) {

        ItemEntity thisAsItemEntity = (ItemEntity) (Object) this;
        if (PreventItemPickupPowerType.doesPrevent(thisAsItemEntity, player)) {
            return false;
        }

        if (original.call(instance, stack)) {
            ActionOnItemPickupPowerType.executeActions(thisAsItemEntity, player);
            return true;
        }

        else {
            return false;
        }

    }

}
