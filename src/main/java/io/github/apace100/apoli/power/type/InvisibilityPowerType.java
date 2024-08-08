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

public class InvisibilityPowerType extends PowerType {

    private final Predicate<Pair<Entity, Entity>> biEntityCondition;

    private final boolean renderArmor;
    private final boolean renderOutline;

    public InvisibilityPowerType(Power power, LivingEntity entity, Predicate<Pair<Entity, Entity>> biEntityCondition, boolean renderArmor, boolean renderOutline) {
        super(power, entity);
        this.biEntityCondition = biEntityCondition;
        this.renderArmor = renderArmor;
        this.renderOutline = renderOutline;
    }

    public boolean doesApply(Entity viewer) {
        return biEntityCondition == null || biEntityCondition.test(new Pair<>(viewer, entity));
    }

    public boolean shouldRenderArmor() {
        return renderArmor;
    }
    
    public boolean shouldRenderOutline() {
        return renderOutline;
    }

    public static PowerTypeFactory<InvisibilityPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("invisibility"),
            new SerializableData()
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                .add("render_armor", SerializableDataTypes.BOOLEAN, false)
                .add("render_outline", SerializableDataTypes.BOOLEAN, false),
            data -> (power, entity) -> new InvisibilityPowerType(power, entity,
                data.get("bientity_condition"),
                data.getBoolean("render_armor"),
                data.getBoolean("render_outline")
            )
        ).allowCondition();
    }
}
