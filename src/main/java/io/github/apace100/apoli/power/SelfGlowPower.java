package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class SelfGlowPower extends Power {

    private final Predicate<Entity> entityCondition;
    private final Predicate<Pair<Entity, Entity>> bientityCondition;
    private final boolean useTeams;
    private final float red;
    private final float green;
    private final float blue;

    public SelfGlowPower(PowerType<?> type, LivingEntity entity, Predicate<Entity> entityCondition, Predicate<Pair<Entity, Entity>> bientityCondition, boolean useTeams, float red, float green, float blue) {
        super(type, entity);
        this.entityCondition = entityCondition;
        this.bientityCondition = bientityCondition;
        this.useTeams = useTeams;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public boolean doesApply(Entity e) {
        return (entityCondition == null || entityCondition.test(e)) && (bientityCondition == null || bientityCondition.test(new Pair<>(e, entity)));
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
