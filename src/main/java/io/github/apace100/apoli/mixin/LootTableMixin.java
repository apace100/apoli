package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.KeyableLootTable;
import io.github.apace100.apoli.access.ReplacingLootContext;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ReplaceLootTablePowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.ReloadableRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(LootTable.class)
public class LootTableMixin implements KeyableLootTable {

    @Unique
    private RegistryKey<LootTable> apoli$lootTableKey;
    @Unique
    private ReloadableRegistries.Lookup apoli$registryLookup;

	@Override
	public RegistryKey<LootTable> apoli$getKey() {
		return apoli$lootTableKey;
	}

    @Override
    public void apoli$setup(RegistryKey<LootTable> lootTableKey, ReloadableRegistries.Lookup lookup) {
        this.apoli$lootTableKey = lootTableKey;
        this.apoli$registryLookup = lookup;
    }

    @Inject(method = "generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void modifyLootTable(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {

        if (!(context instanceof ReplacingLootContext replacingContext) || apoli$getKey() == null || replacingContext.apoli$isReplaced(thisAsLootTable())) {
            return;
        }

        Entity thisEntity = context.get(LootContextParameters.THIS_ENTITY);
        Entity powerHolder = thisEntity;

        LootContextType contextType = replacingContext.apoli$getType();
        if (contextType == LootContextTypes.FISHING) {

            if (thisEntity instanceof FishingBobberEntity fishingBobberEntity) {
                powerHolder = fishingBobberEntity.getOwner();
            }

        }

        else if (contextType == LootContextTypes.ENTITY) {

            if (context.hasParameter(LootContextParameters.ATTACKING_ENTITY)) {
                powerHolder = context.get(LootContextParameters.ATTACKING_ENTITY);
            }

        }

        else if (contextType == LootContextTypes.BARTER) {

            if (thisEntity instanceof PiglinEntity piglinEntity) {

                powerHolder = Optional.ofNullable(piglinEntity.getBrain()
                    .getOptionalMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER))
                    .flatMap(Function.identity())
                    .orElse(null);

            }

        }

        List<ReplaceLootTablePowerType> replaceLootTablePowerTypes = PowerHolderComponent.getPowerTypes(powerHolder, ReplaceLootTablePowerType.class)
            .stream()
            .filter(p -> p.hasReplacement(apoli$getKey()) && p.doesApply(context))
            .sorted(Comparator.comparing(ReplaceLootTablePowerType::getPriority))
            .toList();

        if (replaceLootTablePowerTypes.isEmpty()) {
            return;
        }

        ReplaceLootTablePowerType.addToStack(thisAsLootTable());
        Optional<LootTable> replacementTable = Optional.empty();

        for (ReplaceLootTablePowerType replaceLootTablePowerType : replaceLootTablePowerTypes) {

            replacementTable = replaceLootTablePowerType
                .getReplacement(this.apoli$getKey())
                .map(this.apoli$registryLookup::getLootTable);

            replacementTable.ifPresent(ReplaceLootTablePowerType::addToStack);

        }

        replacingContext.apoli$setReplaced(thisAsLootTable());
        replacementTable.ifPresent(lootTable -> lootTable.generateUnprocessedLoot(context, lootConsumer));

        ReplaceLootTablePowerType.clearStack();
        ci.cancel();

    }

    @Inject(method = "generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/context/LootContext;markActive(Lnet/minecraft/loot/context/LootContext$Entry;)Z"))
    private void popReplacementStack(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {
        ReplaceLootTablePowerType.pop();
    }

    @Inject(method = "generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/context/LootContext;markInactive(Lnet/minecraft/loot/context/LootContext$Entry;)V"))
    private void restoreReplacementStack(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {
        ReplaceLootTablePowerType.restore();
    }

    @Unique
    private LootTable thisAsLootTable() {
        return (LootTable) (Object) this;
    }

}
