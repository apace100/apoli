package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;

import java.util.function.Consumer;

public class ActionOverTimePowerType extends PowerType {

    private final Consumer<Entity> entityAction;
    private final Consumer<Entity> risingAction;
    private final Consumer<Entity> fallingAction;

    private final int interval;

    private Integer startTicks = null;
    private Integer endTicks = null;

    private boolean wasActive = false;

    public ActionOverTimePowerType(Power power, LivingEntity entity, Consumer<Entity> entityAction, Consumer<Entity> risingAction, Consumer<Entity> fallingAction, int interval) {
        super(power, entity);
        this.interval = interval;
        this.entityAction = entityAction;
        this.risingAction = risingAction;
        this.fallingAction = fallingAction;
        this.setTicking(true);
    }

    @Override
    public void tick() {

        if (isActive()) {

            if (startTicks == null) {

                startTicks = entity.age % interval;
                endTicks = null;

                return;

            }

            if (entity.age % interval != startTicks) {
                return;
            }

            if (!wasActive && risingAction != null) {
                risingAction.accept(entity);
            }

            if (entityAction != null) {
                entityAction.accept(entity);
            }

            wasActive = true;

        } else if (wasActive) {

            if (endTicks == null) {

                endTicks = entity.age % interval;
                startTicks = null;

                return;

            }

            if (entity.age % interval != endTicks) {
                return;
            }

            if (fallingAction != null) {
                fallingAction.accept(entity);
            }

            wasActive = false;

        }

    }

    @Override
    public NbtElement toTag() {
        return NbtByte.of(wasActive);
    }

    @Override
    public void fromTag(NbtElement tag) {
        wasActive = tag.equals(NbtByte.ONE);
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("action_over_time"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("rising_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("falling_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("interval", SerializableDataTypes.POSITIVE_INT, 20),
            data -> (power, entity) -> new ActionOverTimePowerType(power, entity,
                data.get("entity_action"),
                data.get("rising_action"),
                data.get("falling_action"),
                data.get("interval")
            )
        ).allowCondition();
    }

}
