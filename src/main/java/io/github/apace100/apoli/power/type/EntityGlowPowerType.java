package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

import java.util.function.Predicate;

public class EntityGlowPowerType extends PowerType {

    private final Predicate<Entity> entityCondition;
    private final Predicate<Pair<Entity, Entity>> biEntityCondition;

    private final boolean useTeams;

    private final float red;
    private final float green;
    private final float blue;

    public EntityGlowPowerType(Power power, LivingEntity entity, Predicate<Entity> entityCondition, Predicate<Pair<Entity, Entity>> biEntityCondition, boolean useTeams, float red, float green, float blue) {
        super(power, entity);
        this.entityCondition = entityCondition;
        this.biEntityCondition = biEntityCondition;
        this.useTeams = useTeams;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public boolean doesApply(Entity e) {
        return (entityCondition == null || entityCondition.test(e))
            && (biEntityCondition == null || biEntityCondition.test(new Pair<>(entity, e)));
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

    public static PowerTypeFactory<EntityGlowPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("entity_glow"),
            new SerializableData()
                .add("entity_condition", ApoliDataTypes.ENTITY_CONDITION, null)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("use_teams", SerializableDataTypes.BOOLEAN, true)
                .add("red", SerializableDataTypes.FLOAT, 1.0F)
                .add("green", SerializableDataTypes.FLOAT, 1.0F)
                .add("blue", SerializableDataTypes.FLOAT, 1.0F),
            data -> (power, entity) -> new EntityGlowPowerType(power, entity,
                data.get("entity_condition"),
                data.get("bientity_condition"),
                data.getBoolean("use_teams"),
                data.getFloat("red"),
                data.getFloat("green"),
                data.getFloat("blue")
            )
        ).allowCondition();
    }
}
