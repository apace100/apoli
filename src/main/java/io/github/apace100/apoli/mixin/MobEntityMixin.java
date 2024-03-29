package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.github.apace100.apoli.power.ActionOnItemPickupPower;
import io.github.apace100.apoli.power.PreventItemPickupPower;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements Targeter {

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

}
