package io.github.apace100.apoli.power;

import io.github.apace100.apoli.util.HudRender;

public interface HudRendered {

    HudRender getRenderSettings();
    float getFill();
    boolean shouldRender();

}
