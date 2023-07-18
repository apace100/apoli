package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.power.ActionOnItemPickupPower;
import io.github.apace100.apoli.power.PreventItemPickupPower;
import io.github.apace100.apoli.power.Prioritized;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {

    @Shadow public abstract ItemStack getStack();

    @Shadow @Nullable private UUID thrower;

    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"), cancellable = true)
    private void apoli$onItemPickup(PlayerEntity player, CallbackInfo ci) {

        ItemStack stack = this.getStack();
        UUID throwerUUID = this.thrower;

        MinecraftServer server = this.getServer();
        if (server == null) {
            return;
        }

        Entity throwerEntity = null;
        if (throwerUUID != null) {
            for (ServerWorld world : server.getWorlds()) {
                if ((throwerEntity = world.getEntity(throwerUUID)) != null) {
                    break;
                }
            }
        }

        final Entity finalThrowerEntity = throwerEntity;

        int preventItemPickupPowers = 0;
        Prioritized.CallInstance<PreventItemPickupPower> pippci = new Prioritized.CallInstance<>();
        pippci.add(player, PreventItemPickupPower.class, p -> p.doesPrevent(stack, finalThrowerEntity));

        for (int i = pippci.getMaxPriority(); i >= pippci.getMinPriority(); i--) {

            if (!pippci.hasPowers(i)) {
                continue;
            }

            List<PreventItemPickupPower> pipps = pippci.getPowers(i);

            preventItemPickupPowers += pipps.size();
            pipps.forEach(p -> p.executeActions((ItemEntity) (Object) this, finalThrowerEntity));

        }

        if (preventItemPickupPowers > 0) {
            ci.cancel();
            return;
        }

        Prioritized.CallInstance<ActionOnItemPickupPower> aoippci = new Prioritized.CallInstance<>();
        aoippci.add(player, ActionOnItemPickupPower.class, p -> p.doesApply(stack, finalThrowerEntity));

        for (int i = aoippci.getMaxPriority(); i >= aoippci.getMinPriority(); i--) {
            aoippci.getPowers(i).forEach(p -> p.executeActions(stack, finalThrowerEntity));
        }

    }

}
