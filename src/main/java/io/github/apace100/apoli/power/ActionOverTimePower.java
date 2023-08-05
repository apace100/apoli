package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;

import java.util.function.Consumer;

public class ActionOverTimePower extends Power {

    private final int interval;
    private final Consumer<Entity> entityAction;
    private final Consumer<Entity> risingAction;
    private final Consumer<Entity> fallingAction;

    private boolean wasActive = false;

    private Integer initialTicks = null;

    public ActionOverTimePower(PowerType<?> type, LivingEntity entity, int interval, Consumer<Entity> entityAction, Consumer<Entity> risingAction, Consumer<Entity> fallingAction) {
        super(type, entity);
        if(interval <= 0) interval = 1;
        this.interval = interval;
        this.entityAction = entityAction;
        this.risingAction = risingAction;
        this.fallingAction = fallingAction;
        this.setTicking(true);
    }

    public void tick() {
        if (initialTicks == null) {
            initialTicks = entity.age % interval;
        }
        else if (entity.age % interval == initialTicks) {
            if (isActive()) {
                if (!wasActive && risingAction != null) {
                    risingAction.accept(entity);
                }
                if (entityAction != null) {
                    entityAction.accept(entity);
                }
                wasActive = true;
            }
            else {
                if (wasActive && fallingAction != null) {
                    fallingAction.accept(entity);
                }
                wasActive = false;
            }
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
        return new PowerFactory<>(Apoli.identifier("action_over_time"),
            new SerializableData()
                .add("interval", SerializableDataTypes.INT, 20)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("rising_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("falling_action", ApoliDataTypes.ENTITY_ACTION, null),
            data ->
                (type, player) -> new ActionOverTimePower(type, player, data.getInt("interval"),
                        data.get("entity_action"), data.get("rising_action"), data.get("falling_action")))
            .allowCondition();
    }
}
