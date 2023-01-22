package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class InvisibilityPower extends Power {

    private final boolean renderArmor;
    private final boolean renderOutline;

    public InvisibilityPower(PowerType<?> type, LivingEntity entity, boolean renderArmor, boolean renderOutline) {
        super(type, entity);
        this.renderArmor = renderArmor;
        this.renderOutline = renderOutline;
    }

    public boolean shouldRenderArmor() {
        return renderArmor;
    }
    
    public boolean shouldRenderOutline() {
        return renderOutline;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("invisibility"),
            new SerializableData()
                .add("render_armor", SerializableDataTypes.BOOLEAN, false)
                .add("render_outline", SerializableDataTypes.BOOLEAN, false),
            data ->
                (type, player) -> new InvisibilityPower(type, player,
                    data.getBoolean("render_armor"),
                    data.getBoolean("render_outline")
                ))
            .allowCondition();
    }
}
