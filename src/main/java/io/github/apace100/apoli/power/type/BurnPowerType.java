package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BurnPowerType extends PowerType {

    public static final TypedDataObjectFactory<BurnPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("burn_duration", SerializableDataTypes.POSITIVE_FLOAT)
            .add("interval", SerializableDataTypes.POSITIVE_INT),
        (data, condition) -> new BurnPowerType(
            data.get("burn_duration"),
            data.get("interval"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("burn_duration", powerType.burnDuration)
            .set("interval", powerType.interval)
    );

    private final float burnDuration;
    private final int interval;

    private Integer startTicks = null;
    private Integer endTicks = null;

    private boolean wasActive = false;

    public BurnPowerType(float burnDuration, int interval, Optional<EntityCondition> condition) {
        super(condition);
        this.interval = interval;
        this.burnDuration = burnDuration;
        this.setTicking(true);
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.BURN;
    }

    @Override
    public void serverTick() {

        if (isActive()) {

            if (startTicks == null) {
                this.startTicks = getHolder().age % interval;
                this.endTicks = null;
            }

            else if (getHolder().age % interval == startTicks) {
                getHolder().setOnFireFor(burnDuration);
                this.wasActive = true;
            }

        }

        else if (wasActive) {

            if (endTicks == null) {
                this.startTicks = null;
                this.endTicks = getHolder().age % interval;
            }

            else if (getHolder().age % interval == endTicks) {
                this.wasActive = false;
            }

        }

    }

}
