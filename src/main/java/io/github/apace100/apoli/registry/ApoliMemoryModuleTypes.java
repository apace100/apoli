package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.Apoli;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ApoliMemoryModuleTypes {
    public static final MemoryModuleType<LivingEntity> ATTACK_TARGET = register(Apoli.identifier("attack_target"));
    public static final MemoryModuleType<Boolean> ATTACK_COOLING_DOWN = register(Apoli.identifier("attack_cooling_down"));
    public static final MemoryModuleType<LivingEntity> AVOID_TARGET = register(Apoli.identifier("avoid_target"));
    public static final MemoryModuleType<LivingEntity> BEHAVIOR_TARGET = register(Apoli.identifier("behavior_target"));

    public static void register() {

    }

    private static <T> MemoryModuleType<T> register(Identifier identifier) {
        return Registry.register(Registries.MEMORY_MODULE_TYPE, identifier, new MemoryModuleType<T>(Optional.empty()));
    }
}
