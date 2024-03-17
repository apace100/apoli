package io.github.apace100.apoli.data;

import io.github.apace100.apoli.Apoli;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public interface ApoliDamageTypes {
    RegistryKey<DamageType> SYNC_DAMAGE_SOURCE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Apoli.identifier("sync_damage_source"));
}
