package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

//  TODO: Use a custom class for storing/specifying colors -eggohito
public class EntityGlowPowerType extends PowerType {

    public static final TypedDataObjectFactory<EntityGlowPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_condition", EntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("use_teams", SerializableDataTypes.BOOLEAN, true)
            .add("red", ApoliDataTypes.NORMALIZED_FLOAT, 1.0F)
            .add("green", ApoliDataTypes.NORMALIZED_FLOAT, 1.0F)
            .add("blue", ApoliDataTypes.NORMALIZED_FLOAT, 1.0F),
        (data, condition) -> new EntityGlowPowerType(
            data.get("entity_condition"),
            data.get("bientity_condition"),
            data.get("use_teams"),
            data.get("red"),
            data.get("green"),
            data.get("blue"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_condition", powerType.entityCondition)
            .set("bientity_condition", powerType.biEntityCondition)
            .set("use_teams", powerType.useTeams)
            .set("red", powerType.red)
            .set("green", powerType.green)
            .set("blue", powerType.blue)
    );

    private final Optional<EntityCondition> entityCondition;
    private final Optional<BiEntityCondition> biEntityCondition;

    private final boolean useTeams;

    private final float red;
    private final float green;
    private final float blue;

    public EntityGlowPowerType(Optional<EntityCondition> entityCondition, Optional<BiEntityCondition> biEntityCondition, boolean useTeams, float red, float green, float blue, Optional<EntityCondition> condition) {
        super(condition);
        this.entityCondition = entityCondition;
        this.biEntityCondition = biEntityCondition;
        this.useTeams = useTeams;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ENTITY_GLOW;
    }

    public boolean doesApply(Entity entity) {
        return entityCondition.map(condition -> condition.test(entity)).orElse(true)
            && biEntityCondition.map(condition -> condition.test(getHolder(), entity)).orElse(true);
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
