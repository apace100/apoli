package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.util.HudRender;
import net.minecraft.entity.LivingEntity;

public class HudRenderedVariableIntPowerType extends VariableIntPowerType implements HudRendered {

    private final HudRender hudRender;

    public HudRenderedVariableIntPowerType(Power power, LivingEntity entity, HudRender hudRender, int startValue, int min, int max) {
        super(power, entity, startValue, min, max);
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
