package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.NameMutableDamageSource;
import net.minecraft.entity.damage.DamageSource;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements NameMutableDamageSource {

    @Unique
    private String apoli$mutableName;

    @Override
    public void setName(String name) {
        apoli$mutableName = name;
    }

    @ModifyVariable(method = "getDeathMessage", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/damage/DamageSource;attacker:Lnet/minecraft/entity/Entity;", ordinal = 0))
    private String apoli$modifyDeathMessageString(String value) {
        if(apoli$mutableName != null) {
            return "death.attack." + apoli$mutableName;
        }
        return value;
    }
}
