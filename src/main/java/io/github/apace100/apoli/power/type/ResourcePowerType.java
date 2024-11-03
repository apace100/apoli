package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ResourcePowerType extends HudRenderedVariableIntPowerType {

    public static final TypedDataObjectFactory<ResourcePowerType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("min_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("max_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("hud_render", HudRender.DATA_TYPE, HudRender.DONT_RENDER)
            .add("min", SerializableDataTypes.INT)
            .add("max", SerializableDataTypes.INT)
            .addFunctionedDefault("start_value", SerializableDataTypes.INT, data -> data.get("min")),
        data -> new ResourcePowerType(
            data.get("min_action"),
            data.get("max_action"),
            data.get("hud_render"),
            data.get("min"),
            data.get("max"),
            data.get("start_value")
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("min_action", powerType.minAction)
            .set("max_action", powerType.maxAction)
            .set("hud_render", powerType.getRenderSettings())
            .set("min", powerType.getMin())
            .set("max", powerType.getMax())
            .set("start_value", powerType.getStartValue())
    );

    private final Optional<EntityAction> minAction;
    private final Optional<EntityAction> maxAction;

    public ResourcePowerType(Optional<EntityAction> minAction, Optional<EntityAction> maxAction, HudRender hudRender, int min, int max, int startValue) {
        super(hudRender, min, max, startValue);
        this.minAction = minAction;
        this.maxAction = maxAction;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.RESOURCE;
    }

    @Override
    public int setValue(int newValue) {

        int oldValue = getValue();
        int actualNewValue = super.setValue(newValue);

        if (oldValue != actualNewValue) {
            minAction.filter(action -> actualNewValue == getMin()).ifPresent(action -> action.execute(getHolder()));
            maxAction.filter(action -> actualNewValue == getMax()).ifPresent(action -> action.execute(getHolder()));
        }

        return actualNewValue;

    }

}
