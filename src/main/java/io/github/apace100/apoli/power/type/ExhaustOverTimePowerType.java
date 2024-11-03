package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExhaustOverTimePowerType extends PowerType {

    public static final TypedDataObjectFactory<ExhaustOverTimePowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("interval", SerializableDataTypes.POSITIVE_INT, 20)
            .add("exhaustion", SerializableDataTypes.FLOAT),
        (data, condition) -> new ExhaustOverTimePowerType(
            data.get("interval"),
            data.get("exhaustion"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("interval", powerType.exhaustInterval)
            .set("exhaustion", powerType.exhaustion)
    );

    private final int exhaustInterval;
    private final float exhaustion;

    private Integer startTicks = null;
    private Integer endTicks = null;

    private boolean wasActive = false;

    public ExhaustOverTimePowerType(int exhaustInterval, float exhaustion, Optional<EntityCondition> condition) {
        super(condition);
        this.exhaustInterval = exhaustInterval;
        this.exhaustion = exhaustion;
        this.setTicking(true);
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.EXHAUST;
    }

    @Override
    public void serverTick() {

        if (!(getHolder() instanceof PlayerEntity holderPlayer)) {
            return;
        }

        if (isActive()) {

            if (startTicks == null) {
                this.startTicks = holderPlayer.age % exhaustInterval;
                this.endTicks = null;
            }

            else if (holderPlayer.age % exhaustInterval == startTicks) {
                holderPlayer.addExhaustion(exhaustion);
                this.wasActive = true;
            }

        }

        else if (wasActive) {

            if (endTicks == null) {
                this.startTicks = null;
                this.endTicks = holderPlayer.age % exhaustInterval;
            }

            else if (holderPlayer.age % exhaustInterval == endTicks) {
                this.wasActive = false;
            }

        }

    }

}
