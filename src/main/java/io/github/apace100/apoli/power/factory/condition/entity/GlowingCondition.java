package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

public class GlowingCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {
        return !entity.getWorld().isClient ? entity.isGlowing() : MinecraftClient.getInstance().hasOutline(entity);
    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("glowing"),
            new SerializableData(),
            GlowingCondition::condition
        );
    }

}
