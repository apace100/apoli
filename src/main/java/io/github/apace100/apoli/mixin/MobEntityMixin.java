package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.apace100.apoli.access.LeashableEntity;
import io.github.apace100.apoli.power.ActionOnItemPickupPower;
import io.github.apace100.apoli.power.PreventItemPickupPower;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements Targeter, LeashableEntity {

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapWithCondition(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;loot(Lnet/minecraft/entity/ItemEntity;)V"))
    private boolean apoli$onItemPickup(MobEntity instance, ItemEntity itemEntity) {

        if (PreventItemPickupPower.doesPrevent(itemEntity, this)) {
            return false;
        }

        ActionOnItemPickupPower.executeActions(itemEntity, this);
        return true;

    }

    @Unique
    private boolean apoli$customLeashed;

    @Override
    public boolean apoli$isCustomLeashed() {
        return apoli$customLeashed;
    }

    @Override
    public void apoli$setCustomLeashed(boolean customLeashed) {
        this.apoli$customLeashed = customLeashed;
    }

    @WrapWithCondition(method = "detachLeash", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;dropItem(Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/entity/ItemEntity;"))
    private boolean apoli$preventDroppingLeashOnCustom(MobEntity mob, ItemConvertible item) {
        return !this.apoli$isCustomLeashed();
    }

    @Inject(method = "detachLeash", at = @At("TAIL"))
    private void apoli$resetCustomLeashStatus(boolean sendPacket, boolean dropItem, CallbackInfo ci) {
        this.apoli$setCustomLeashed(false);
    }

}
