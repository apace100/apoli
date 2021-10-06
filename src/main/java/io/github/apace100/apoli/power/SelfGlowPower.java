package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
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

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("self_glow"),
            new SerializableData()
                .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("use_teams", SerializableDataTypes.BOOLEAN, true)
                .add("red", SerializableDataTypes.FLOAT, 1.0F)
                .add("green", SerializableDataTypes.FLOAT, 1.0F)
                .add("blue", SerializableDataTypes.FLOAT, 1.0F),
            data ->
                (type, player) -> new SelfGlowPower(type, player,
                    (ConditionFactory<Entity>.Instance)data.get("entity_condition"),
                    (ConditionFactory<Pair<Entity, Entity>>.Instance)data.get("bientity_condition"),
                    data.getBoolean("use_teams"),
                    data.getFloat("red"),
                    data.getFloat("green"),
                    data.getFloat("blue")))
            .allowCondition();
    }
}
