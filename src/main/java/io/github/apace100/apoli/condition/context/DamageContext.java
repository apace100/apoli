package io.github.apace100.apoli.condition.context;

import net.minecraft.entity.damage.DamageSource;

public record DamageContext(DamageSource source, float amount) {

}
