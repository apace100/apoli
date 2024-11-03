package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class InvisibilityPowerType extends PowerType {

    public static final TypedDataObjectFactory<InvisibilityPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("render_armor", SerializableDataTypes.BOOLEAN, false)
            .add("render_outline", SerializableDataTypes.BOOLEAN, false),
        (data, condition) -> new InvisibilityPowerType(
            data.get("bientity_condition"),
            data.get("render_armor"),
            data.get("render_outline"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("bientity_condition", powerType.biEntityCondition)
            .set("render_armor", powerType.renderArmor)
            .set("render_outline", powerType.renderOutline)
    );

    private final Optional<BiEntityCondition> biEntityCondition;

    private final boolean renderArmor;
    private final boolean renderOutline;

    public InvisibilityPowerType(Optional<BiEntityCondition> biEntityCondition, boolean renderArmor, boolean renderOutline, Optional<EntityCondition> condition) {
        super(condition);
        this.biEntityCondition = biEntityCondition;
        this.renderArmor = renderArmor;
        this.renderOutline = renderOutline;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.INVISIBILITY;
    }

    public boolean doesApply(Entity viewer) {
        return biEntityCondition
            .map(condition -> condition.test(viewer, getHolder()))
            .orElse(true);
    }

    public boolean shouldRenderArmor() {
        return renderArmor;
    }
    
    public boolean shouldRenderOutline() {
        return renderOutline;
    }

}
