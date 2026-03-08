package io.github.apace100.apoli.mixin.power.type;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.access.KeyableLootTable;
import io.github.apace100.apoli.access.LootContextTypeHolder;
import io.github.apace100.apoli.access.ReplacingLootContext;
import io.github.apace100.apoli.power.type.Prioritized;
import io.github.apace100.apoli.power.type.ReplaceLootTablePowerType;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.*;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.ReloadableRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class ReplaceLootTablePowerTypeMixin {

	@Mixin(ReloadableRegistries.Lookup.class)
	public static abstract class Replacer {

		@Inject(method = "<init>", at = @At("TAIL"))
		private void setupLootTables(DynamicRegistryManager.Immutable registryManager, CallbackInfo ci) {
			registryManager.get(RegistryKeys.LOOT_TABLE).streamEntries().forEach(reference -> {

				RegistryKey<LootTable> key = reference.registryKey();

				if (reference.value() instanceof KeyableLootTable keyable) {
					keyable.apoli$setup(key, (ReloadableRegistries.Lookup) (Object) this);
				}

			});
		}

		@ModifyReturnValue(method = "getLootTable", at = @At("RETURN"))
		private LootTable getReplacedOrNormalTable(LootTable original, RegistryKey<LootTable> key) {

			if (key.equals(ReplaceLootTablePowerType.REPLACED_TABLE_KEY)) {
				return ReplaceLootTablePowerType.peek();
			}

			else {
				return original;
			}

		}

	}

	@Mixin(LootTableEntry.class)
	public static abstract class NestedReplacer {

		@SuppressWarnings("unchecked")
		@WrapOperation(method = "generateLoot", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Either;map(Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/lang/Object;"))
		private <T, L extends RegistryKey<LootTable>, R extends LootTable> T replaceGetter(Either<L, R> either, Function<? super L, ? extends T> leftFunction, Function<? super R, ? extends T> rightFunction, Operation<T> original, Consumer<ItemStack> stackConsumer, LootContext lootContext) {

			ReloadableRegistries.Lookup lookup = lootContext.getWorld().getServer().getReloadableRegistries();
			Function<? super L, ? extends T> newGetter = l -> (T) lookup.getLootTable(l);

			return original.call(either, newGetter, rightFunction);

		}

	}

	@Mixin(LootTable.class)
	public static abstract class LootTableCache implements KeyableLootTable {

		@Unique
		private RegistryKey<LootTable> apoli$key;

		@Unique
		private ReloadableRegistries.Lookup apoli$lookup;

		@Override
		public RegistryKey<LootTable> apoli$getKey() {
			return apoli$key;
		}

		@Override
		public void apoli$setup(RegistryKey<LootTable> lootTableKey, ReloadableRegistries.Lookup lookup) {
			this.apoli$key = lootTableKey;
			this.apoli$lookup = lookup;
		}

		@Inject(method = "generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
		private void replaceTable(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {

			if (!(context instanceof ReplacingLootContext replacingContext)) {
				return;
			}

			LootContextType contextType = replacingContext.apoli$getType();
			RegistryKey<LootTable> key = this.apoli$getKey();

			if (key == null || replacingContext.apoli$isReplaced(key)) {
				return;
			}

			Entity thisEntity = context.get(LootContextParameters.THIS_ENTITY);
			Entity holder = thisEntity;

			if (contextType == LootContextTypes.FISHING) {

				if (thisEntity instanceof FishingBobberEntity bobber) {
					holder = bobber.getOwner();
				}

			}

			else if (contextType == LootContextTypes.ENTITY) {

				if (context.hasParameter(LootContextParameters.ATTACKING_ENTITY)) {
					holder = context.get(LootContextParameters.ATTACKING_ENTITY);
				}

			}

			else if (contextType == LootContextTypes.BARTER) {

				if (thisEntity instanceof PiglinEntity piglin) {
					holder = piglin.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).orElse(null);
				}

			}

			ReplaceLootTablePowerType.push((LootTable) (Object) this);
			Prioritized.CallInstance<ReplaceLootTablePowerType> types = new Prioritized.CallInstance<>();

			Optional<LootTable> replacementTable = Optional.empty();
			types.add(holder, ReplaceLootTablePowerType.class, type -> type.hasReplacement(key) && type.doesApply(context));

			for (int priority = types.getMaxPriority(); priority >= types.getMinPriority(); priority--) {

				for (var type : types.getPowerTypes(priority)) {

					replacementTable = type.getReplacement(key)
						.map(this.apoli$lookup::getLootTable)
						.filter(Predicate.not(LootTable.EMPTY::equals));

				}

			}

			if (replacementTable.isEmpty()) {
				return;
			}

			LootTable table = replacementTable.get();
			replacingContext.apoli$setReplaced(key);

			table.generateUnprocessedLoot(context, lootConsumer);
			ci.cancel();

		}

		@WrapMethod(method = "generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V")
		private void wrapGenerateForReplacing(LootContext context, Consumer<ItemStack> lootConsumer, Operation<Void> original) {

			try {
				original.call(context, lootConsumer);
			}

			finally {
				ReplaceLootTablePowerType.clear();
			}

		}

		@Inject(method = "generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/context/LootContext;markActive(Lnet/minecraft/loot/context/LootContext$Entry;)Z"))
		private void popReplaced(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {
			ReplaceLootTablePowerType.pop();
		}

		@Inject(method = "generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/context/LootContext;markInactive(Lnet/minecraft/loot/context/LootContext$Entry;)V"))
		private void restoreReplaced(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {
			ReplaceLootTablePowerType.restore();
		}

	}

	@Mixin(LootContext.class)
	public static abstract class LootContextCache implements ReplacingLootContext {

		@Shadow
		@Final
		private LootContextParameterSet parameters;

		@Unique
		private final Set<RegistryKey<LootTable>> apoli$replacedTables = new ObjectOpenHashSet<>();

		@Override
		public LootContextType apoli$getType() {
			return ((LootContextTypeHolder) this.parameters).apoli$getType();
		}

		@Override
		public boolean apoli$isReplaced(RegistryKey<LootTable> key) {
			return apoli$replacedTables.contains(key);
		}

		@Override
		public void apoli$setReplaced(RegistryKey<LootTable> key) {
			this.apoli$replacedTables.add(key);
		}

	}

	@Mixin(LootContextParameterSet.class)
	public static abstract class LootContextParametersCache implements LootContextTypeHolder {

		@Unique
		private LootContextType apoli$contextType;

		@Override
		public LootContextType apoli$getType() {
			return Objects.requireNonNull(this.apoli$contextType, "Loot context parameters are not initialized properly!");
		}

		@Override
		public void apoli$setType(LootContextType type) {
			this.apoli$contextType = type;
		}

	}

	@Mixin(LootContextParameterSet.Builder.class)
	public static abstract class LootContextParametersCacheInit {

		@ModifyReturnValue(method = "build", at = @At("RETURN"))
		private LootContextParameterSet cacheType(LootContextParameterSet original, LootContextType type) {

			((LootContextTypeHolder) original).apoli$setType(type);

			return original;

		}

	}

}
