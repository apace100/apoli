package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.util.HudRender;

public interface HudRendered {

    HudRender getRenderSettings();

    float getFill();
    boolean shouldRender();

}
