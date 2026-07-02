package io.github.apace100.apoli.mixin.power.type;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.power.type.PreventDeathPowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class PreventDeathPowerTypeMixin extends Entity {

	PreventDeathPowerTypeMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@WrapOperation(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tryUseTotem(Lnet/minecraft/entity/damage/DamageSource;)Z"))
	boolean preventDeath(LivingEntity entity, DamageSource source, Operation<Boolean> original, @Local(argsOnly = true) float amount) {
		return PreventDeathPowerType.doesPrevent(entity, source, amount)
			|| original.call(entity, source);
	}

}
