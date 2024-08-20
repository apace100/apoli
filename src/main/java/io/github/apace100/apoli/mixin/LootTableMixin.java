package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.IdentifiedLootTable;
import io.github.apace100.apoli.access.ReplacingLootContext;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ReplaceLootTablePowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
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

@Mixin(LootTable.class)
public class LootTableMixin implements IdentifiedLootTable {

    @Unique
    private RegistryKey<LootTable> apoli$lootTableKey;
    @Unique
    private ReloadableRegistries.Lookup apoli$registryLookup;

    @Override
    public void apoli$setKey(RegistryKey<LootTable> lootTableKey, ReloadableRegistries.Lookup registryLookup) {
        this.apoli$lootTableKey = lootTableKey;
        this.apoli$registryLookup = registryLookup;
    }

    @Override
    public RegistryKey<LootTable> apoli$getLootTableKey() {
        return apoli$lootTableKey;
    }

    @Inject(method = "generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void modifyLootTable(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {

        if (!(context instanceof ReplacingLootContext replacingContext) || replacingContext.apoli$isReplaced((LootTable) (Object) this)) {
            return;
        }

        if (this.apoli$getLootTableKey() == null || !context.hasParameter(LootContextParameters.THIS_ENTITY)) {
            return;
        }

        LootContextType lootContextType = ((ReplacingLootContext) context).apoli$getType();
        Entity powerHolder = context.get(LootContextParameters.THIS_ENTITY);

        if (lootContextType == LootContextTypes.FISHING) {
            if (powerHolder instanceof FishingBobberEntity fishingBobberEntity) {
                powerHolder = fishingBobberEntity.getOwner();
            }
        } else if (lootContextType == LootContextTypes.ENTITY) {
            if (context.hasParameter(LootContextParameters.ATTACKING_ENTITY)) {
                powerHolder = context.get(LootContextParameters.ATTACKING_ENTITY);
            }
        } else if (lootContextType == LootContextTypes.BARTER) {
            if (powerHolder instanceof PiglinEntity piglinEntity) {

                Optional<PlayerEntity> playerEntity = piglinEntity.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);

                if (playerEntity != null && playerEntity.isPresent()) {
                    powerHolder = playerEntity.get();
                }

            }
        }

        List<ReplaceLootTablePowerType> replaceLootTablePowers = PowerHolderComponent.getPowerTypes(powerHolder, ReplaceLootTablePowerType.class)
            .stream()
            .filter(p -> p.hasReplacement(apoli$lootTableKey) & p.doesApply(context))
            .sorted(Comparator.comparing(ReplaceLootTablePowerType::getPriority))
            .toList();

        if (replaceLootTablePowers.isEmpty()) {
            return;
        }

        ReplaceLootTablePowerType.addToStack((LootTable) (Object) this);
        LootTable replacement = null;

        for (ReplaceLootTablePowerType replaceLootTablePower : replaceLootTablePowers) {

            RegistryKey<LootTable> replacementLootTableKey = replaceLootTablePower.getReplacement(apoli$lootTableKey);
            if (replacementLootTableKey == null) {
                continue;
            }

            replacement = apoli$registryLookup.getLootTable(replacementLootTableKey);
            ReplaceLootTablePowerType.addToStack(replacement);

        }

        if (replacement != null) {
            ((ReplacingLootContext) context).apoli$setReplaced((LootTable) (Object) this);
            replacement.generateUnprocessedLoot(context, lootConsumer);
        }

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

}
