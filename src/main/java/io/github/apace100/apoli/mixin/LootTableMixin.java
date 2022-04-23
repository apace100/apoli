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
    public void setId(Identifier id, LootManager lootManager) {
        apoli$id = id;
        apoli$lootManager = lootManager;
    }

    @Inject(method = "generateUnprocessedLoot", at = @At("HEAD"), cancellable = true)
    private void modifyLootTable(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {
        if(((ReplacingLootContext)context).isReplaced((LootTable)(Object)this)) {
            return;
        }
        if(context.hasParameter(LootContextParameters.THIS_ENTITY)) {
            LootContextType type = ((ReplacingLootContext)context).getType();
            Entity entity = context.get(LootContextParameters.THIS_ENTITY);
            if(type == LootContextTypes.FISHING) {
                if(entity instanceof FishingBobberEntity bobber) {
                    entity = bobber.getPlayerOwner();
                }
            } else if(type == LootContextTypes.ENTITY) {
                if(context.hasParameter(LootContextParameters.KILLER_ENTITY)) {
                    entity = context.get(LootContextParameters.KILLER_ENTITY);
                }
            } else if(type == LootContextTypes.BARTER) {
                if(entity instanceof PiglinEntity piglin) {
                    Optional<PlayerEntity> optional = piglin.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
                    if(optional.isPresent()) {
                        entity = optional.get();
                    }
                }
            }
            List<ReplaceLootTablePower> powers = PowerHolderComponent.getPowers(entity, ReplaceLootTablePower.class);
            Optional<ReplaceLootTablePower> match = powers.stream().filter(p -> p.hasReplacement(apoli$id)).findFirst();
            if(match.isPresent()) {
                ((ReplacingLootContext)context).setReplaced((LootTable)(Object)this);
                Identifier id = match.get().getReplacement(apoli$id);
                LootTable replacement = apoli$lootManager.getTable(id);
                ReplaceLootTablePower.LAST_REPLACED_TABLE_ID = apoli$id;
                replacement.generateUnprocessedLoot(context, lootConsumer);
                ci.cancel();
            }
        }
    }
}
