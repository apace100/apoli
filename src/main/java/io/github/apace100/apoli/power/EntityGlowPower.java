package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class EntityGlowPower extends Power {

    private final Predicate<LivingEntity> entityCondition;
    private final Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition;
    private final boolean useTeams;
    private final float red;
    private final float green;
    private final float blue;

    public EntityGlowPower(PowerType<?> type, LivingEntity entity, Predicate<LivingEntity> entityCondition, Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition, boolean useTeams, float red, float green, float blue) {
        super(type, entity);
        this.entityCondition = entityCondition;
        this.bientityCondition = bientityCondition;
        this.useTeams = useTeams;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public boolean doesApply(Entity e) {
        return e instanceof LivingEntity && (entityCondition == null || entityCondition.test((LivingEntity)e)) && (bientityCondition == null || bientityCondition.test(new Pair<>(entity, (LivingEntity)e)));
    }

    public boolean usesTeams() {
        return useTeams;
    }

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }
}
