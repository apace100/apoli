package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.ReloadableRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unchecked")
@Mixin(LootTableEntry.class)
public abstract class LootTableEntryMixin {

	@WrapOperation(method = "generateLoot", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;map(Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/lang/Object;"))
	private <T, L extends RegistryKey<LootTable>, R extends LootTable> T test(Either<L, R> either, Function<? super L, ? extends T> leftFunction, Function<? super R, ? extends T> rightFunction, Operation<T> original, Consumer<ItemStack> lootConsumer, LootContext context) {

		ReloadableRegistries.Lookup reloadableRegistries = context.getWorld().getServer().getReloadableRegistries();
		Function<? super L, ? extends T> replacedLeftFunction = l -> (T) reloadableRegistries.getLootTable(l);

		return original.call(either, replacedLeftFunction, rightFunction);

	}

}
