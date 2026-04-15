package io.github.apace100.apoli.mixin.power.type;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import io.github.apace100.apoli.util.WorkableEmptyStack;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.item.ComponentSubPredicate;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

public abstract class ModifyEnchantmentLevelPowerTypeMixin {

	@Mixin(EnchantmentHelper.class)
	public static abstract class EnchantmentHelperMixin {

		@WrapOperation(method = "getLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/ItemEnchantmentsComponent;getLevel(Lnet/minecraft/registry/entry/RegistryEntry;)I"))
		private static int apoli$modifyOnLevelQuery(ItemEnchantmentsComponent instance, RegistryEntry<Enchantment> enchantment, Operation<Integer> original, @Local(argsOnly = true) net.minecraft.item.ItemStack stack) {
			return original.call(ModifyEnchantmentLevelPowerType.updateAndGetEnchantments(stack, instance), enchantment);
		}

		@ModifyExpressionValue(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/enchantment/EnchantmentHelper$ContextAwareConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
		private static boolean apoli$allowWorkableEmptiesInIteration(boolean original, net.minecraft.item.ItemStack stack) {
			return original
				&& !WorkableEmptyStack.isOf(stack);
		}

		@ModifyVariable(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;)V", at = @At("STORE"))
		private static ItemEnchantmentsComponent apoli$modifyEnchantmentsOnIteration(ItemEnchantmentsComponent original, net.minecraft.item.ItemStack stack) {
			return ModifyEnchantmentLevelPowerType.updateAndGetEnchantments(stack, original);
		}

		@ModifyVariable(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/enchantment/EnchantmentHelper$ContextAwareConsumer;)V", at = @At("STORE"))
		private static ItemEnchantmentsComponent apoli$modifyEnchantmentsOnForEachWithContext(ItemEnchantmentsComponent original, net.minecraft.item.ItemStack stack) {
			return ModifyEnchantmentLevelPowerType.updateAndGetEnchantments(stack, original);
		}

		@ModifyVariable(method = "hasAnyEnchantmentsIn", at = @At("STORE"))
		private static ItemEnchantmentsComponent apoli$modifyEnchantmentsOnInTagQuery(ItemEnchantmentsComponent original, net.minecraft.item.ItemStack stack) {
			return ModifyEnchantmentLevelPowerType.updateAndGetEnchantments(stack, original);
		}

	}

	@Mixin(value = ItemStack.class, priority = 1001)
	public static abstract class ItemStackMixin implements EntityLinkedItemStack {

		@ModifyReturnValue(method = "copy", at = @At("RETURN"))
		private ItemStack moveCacheToCopy(ItemStack original) {
			return ModifyEnchantmentLevelPowerType.moveCache((ItemStack) (Object) this, original);
		}

	}

	@Mixin(ComponentSubPredicate.class)
	public interface ComponentSubPredicateMixin {

		/**
		 *
		 *  <p>The resulting object had to be manually cast to {@link ItemEnchantmentsComponent}, but that shouldn't
		 *  cause any issues since it's already being checked if the component type is
		 *  {@link DataComponentTypes#ENCHANTMENTS}.</p>
		 *
		 *  <p>This has to be done since it's impossible to check of the object is an instance of
		 *  {@link ItemEnchantmentsComponent} since that wouldn't work if it's null.</p>
		 */
		@WrapOperation(method = "test(Lnet/minecraft/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;get(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;"))
		private Object apoli$accountForModifiedEnchantments(net.minecraft.item.ItemStack stack, ComponentType<?> componentType, Operation<Object> original) {
			Object component = original.call(stack, componentType);
			return componentType == DataComponentTypes.ENCHANTMENTS
				? ModifyEnchantmentLevelPowerType.getEnchantmentsOrElse(stack, (ItemEnchantmentsComponent) component, true)
				: component;
		}

	}

}
