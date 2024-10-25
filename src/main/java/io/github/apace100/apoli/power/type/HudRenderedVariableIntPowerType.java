package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.util.HudRender;

public abstract class HudRenderedVariableIntPowerType extends VariableIntPowerType implements HudRendered {

    private final HudRender hudRender;

    public HudRenderedVariableIntPowerType(HudRender hudRender, int min, int max, int startValue) {
        super(startValue, min, max);
        this.hudRender = hudRender;
    }

    @Override
    public HudRender getRenderSettings() {
        return hudRender;
    }

    @Override
    public float getFill() {
        return (this.getValue() - this.getMin()) / (float)(this.getMax() - this.getMin());
    }

    @Override
    public boolean shouldRender() {
        return true;
    }

}
