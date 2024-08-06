package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.RestrictArmorPower;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.ArmorSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorSlot.class)
public abstract class ArmorSlotMixin {

    @Shadow @Final private LivingEntity entity;

    @Shadow @Final private EquipmentSlot equipmentSlot;

    @ModifyReturnValue(method = "canInsert", at = @At("RETURN"))
    private boolean apoli$preventArmorInsertion(boolean original, ItemStack stack) {
        return original
            && !PowerHolderComponent.hasPower(this.entity, RestrictArmorPower.class, p -> p.doesRestrict(stack, this.equipmentSlot));
    }

}
