package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.NameMutableDamageSource;
import net.minecraft.entity.damage.DamageSource;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Optional;

@Mixin(DamageSource.class)
public abstract class DamageSourceMixin implements NameMutableDamageSource {

    @Shadow
    public abstract String getName();

    @Unique
    private String apoli$mutableName;

    @Override
    public Optional<String> apoli$getName() {
        return Optional.ofNullable(apoli$mutableName);
    }

    @Override
    public void apoli$setName(String name) {
        apoli$mutableName = name;
    }

    @ModifyVariable(method = "getDeathMessage", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/damage/DamageSource;attacker:Lnet/minecraft/entity/Entity;", ordinal = 0))
    private String apoli$modifyDeathMessageString(String value) {
        return "death.attack." + this.getName();
    }

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private String apoli$overrideName(String original) {
        return this.apoli$getName().orElse(original);
    }

}
