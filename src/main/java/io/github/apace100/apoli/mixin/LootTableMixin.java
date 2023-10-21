package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.IdentifiedLootTable;
import io.github.apace100.apoli.access.ReplacingLootContext;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ReplaceLootTablePower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.util.Identifier;
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
    private Identifier apoli$id;
    @Unique
    private LootManager apoli$lootManager;

    @Override
    public void apoli$setId(Identifier id, LootManager lootManager) {
        apoli$id = id;
        apoli$lootManager = lootManager;
    }

    @Override
    public Identifier apoli$getId() {
        return apoli$id;
    }

    @Inject(method = "generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private void modifyLootTable(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {

        if (((ReplacingLootContext) context).apoli$isReplaced((LootTable) (Object) this)) {
            return;
        }

        if (!context.hasParameter(LootContextParameters.THIS_ENTITY)) {
            return;
        }

        LootContextType lootContextType = ((ReplacingLootContext) context).apoli$getType();
        Entity entity = context.get(LootContextParameters.THIS_ENTITY);

        if (lootContextType == LootContextTypes.FISHING) {
            if (entity instanceof FishingBobberEntity fishingBobberEntity) {
                entity = fishingBobberEntity.getOwner();
            }
        } else if (lootContextType == LootContextTypes.ENTITY) {
            if (context.hasParameter(LootContextParameters.KILLER_ENTITY)) {
                entity = context.get(LootContextParameters.KILLER_ENTITY);
            }
        } else if (lootContextType == LootContextTypes.BARTER) {
            if (entity instanceof PiglinEntity piglinEntity) {

                Optional<PlayerEntity> playerEntity = piglinEntity.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);

                if (playerEntity != null && playerEntity.isPresent()) {
                    entity = playerEntity.get();
                }

            }
        }

        List<ReplaceLootTablePower> replaceLootTablePowers = PowerHolderComponent.getPowers(entity, ReplaceLootTablePower.class)
            .stream()
            .filter(p -> p.hasReplacement(apoli$id) && p.doesApply(context))
            .sorted(Comparator.comparing(ReplaceLootTablePower::getPriority))
            .toList();

        if (replaceLootTablePowers.isEmpty()) {
            return;
        }

        ReplaceLootTablePower.addToStack((LootTable) (Object) this);
        LootTable replacement = null;

        for (ReplaceLootTablePower replaceLootTablePower : replaceLootTablePowers) {

            Identifier replacementId = replaceLootTablePower.getReplacement(apoli$id);
            if (replacementId == null) {
                continue;
            }

            replacement = apoli$lootManager.getLootTable(replacementId);
            ReplaceLootTablePower.addToStack(replacement);

        }

        if (replacement != null) {
            ((ReplacingLootContext) context).apoli$setReplaced((LootTable) (Object) this);
            replacement.generateUnprocessedLoot(context, lootConsumer);
        }

        ReplaceLootTablePower.clearStack();
        ci.cancel();

    }

    @Inject(method = "generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/context/LootContext;markActive(Lnet/minecraft/loot/context/LootContext$Entry;)Z"))
    private void popReplacementStack(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {
        ReplaceLootTablePower.pop();
    }

    @Inject(method = "generateUnprocessedLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/context/LootContext;markInactive(Lnet/minecraft/loot/context/LootContext$Entry;)V"))
    private void restoreReplacementStack(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {
        ReplaceLootTablePower.restore();
    }

}
