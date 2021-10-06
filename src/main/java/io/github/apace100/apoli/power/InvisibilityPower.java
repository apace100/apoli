package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;

public class InvisibilityPower extends Power {

    private final boolean renderArmor;

    public InvisibilityPower(PowerType<?> type, LivingEntity entity, boolean renderArmor) {
        super(type, entity);
        this.renderArmor = renderArmor;
    }

    public boolean shouldRenderArmor() {
        return renderArmor;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("invisibility"),
            new SerializableData()
                .add("render_armor", SerializableDataTypes.BOOLEAN),
            data ->
                (type, player) -> new InvisibilityPower(type, player, data.getBoolean("render_armor")))
            .allowCondition();
    }
}
