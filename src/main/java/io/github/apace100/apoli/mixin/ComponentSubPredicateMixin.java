package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ComponentSubPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComponentSubPredicate.class)
public interface ComponentSubPredicateMixin {

    @WrapOperation(method = "test(Lnet/minecraft/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"))
    private Object apoli$accountForModifiedEnchantments(ItemStack stack, ComponentType<?> componentType, Operation<Object> original) {

        Object objComponent = original.call(stack, componentType);
        if (componentType == DataComponentTypes.ENCHANTMENTS) {
            //  The resulting object had to be cast to ItemEnchantmentsComponent, but that should be fine since we're checking if the component
            //  type is enchantments anyway, right...? We can't even check if the object is an instance of it since that would just not work if
            //  it's null... -eggohito
            return ModifyEnchantmentLevelPower.getEnchantments(stack, (ItemEnchantmentsComponent) objComponent, true);
        }

        else {
            return objComponent;
        }

    }

}
