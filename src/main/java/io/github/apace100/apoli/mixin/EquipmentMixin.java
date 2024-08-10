package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.RestrictArmorPowerType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Equipment.class)
public interface EquipmentMixin {

    @ModifyExpressionValue(method = "equipAndSwap", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;canUseSlot(Lnet/minecraft/entity/EquipmentSlot;)Z"))
    private boolean apoli$preventArmorEquipping(boolean original, Item item, World world, PlayerEntity user, @Local ItemStack stack, @Local EquipmentSlot slot) {
        return original
            && !PowerHolderComponent.hasPowerType(user, RestrictArmorPowerType.class, p -> p.doesRestrict(stack, slot));
    }

}
