package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;

import java.util.function.Consumer;

public class ActionOverTimePower extends Power {

    private final Consumer<Entity> entityAction;
    private final Consumer<Entity> risingAction;
    private final Consumer<Entity> fallingAction;

    private final int interval;

    private Integer startTicks = null;
    private Integer endTicks = null;

    private boolean wasActive = false;

    public ActionOverTimePower(PowerType<?> type, LivingEntity entity, int interval, Consumer<Entity> entityAction, Consumer<Entity> risingAction, Consumer<Entity> fallingAction) {
        super(type, entity);
        this.interval = Math.max(interval, 1);
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

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("action_over_time"),
            new SerializableData()
                .add("interval", SerializableDataTypes.INT, 20)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("rising_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("falling_action", ApoliDataTypes.ENTITY_ACTION, null),
            data -> (powerType, livingEntity) -> new ActionOverTimePower(
                powerType,
                livingEntity,
                data.get("interval"),
                data.get("entity_action"),
                data.get("rising_action"),
                data.get("falling_action")
            )
        ).allowCondition();
    }

}
