package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.function.Consumer;

public class ResourcePower extends HudRenderedVariableIntPower {

    private final Consumer<Entity> actionOnMin;
    private final Consumer<Entity> actionOnMax;

    public ResourcePower(PowerType<?> type, LivingEntity entity, HudRender hudRender, int startValue, int min, int max, Consumer<Entity> actionOnMin, Consumer<Entity> actionOnMax) {
        super(type, entity, hudRender, startValue, min, max);
        this.actionOnMin = actionOnMin;
        this.actionOnMax = actionOnMax;
    }

    @Override
    public int setValue(int newValue) {
        int oldValue = currentValue;
        int actualNewValue = super.setValue(newValue);
        if(oldValue != actualNewValue) {
            if(actionOnMin != null && actualNewValue == min) {
                actionOnMin.accept(entity);
            }
            if(actionOnMax != null && actualNewValue == max) {
                actionOnMax.accept(entity);
            }
        }
        return actualNewValue;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("resource"),
            new SerializableData()
                .add("min", SerializableDataTypes.INT)
                .add("max", SerializableDataTypes.INT)
                .addFunctionedDefault("start_value", SerializableDataTypes.INT, data -> data.getInt("min"))
                .add("hud_render", ApoliDataTypes.HUD_RENDER)
                .add("min_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("max_action", ApoliDataTypes.ENTITY_ACTION, null),
            data ->
                (type, player) ->
                    new ResourcePower(type, player,
                        (HudRender)data.get("hud_render"),
                        data.getInt("start_value"),
                        data.getInt("min"),
                        data.getInt("max"),
                        (ActionFactory<Entity>.Instance)data.get("min_action"),
                        (ActionFactory<Entity>.Instance)data.get("max_action")))
            .allowCondition();
    }
}
