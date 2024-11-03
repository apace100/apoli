package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.HudRender;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class ActiveCooldownPowerType extends CooldownPowerType implements Active {

    private final Key key;

    public ActiveCooldownPowerType(HudRender hudRender, int cooldownDuration, Key key, Optional<EntityCondition> condition) {
        super(cooldownDuration, hudRender, condition);
        this.key = key;
    }

    public ActiveCooldownPowerType(HudRender hudRender, int cooldownDuration, Key key) {
        this(hudRender, cooldownDuration, key, Optional.empty());
    }

    @Override
    public abstract @NotNull PowerConfiguration<?> getConfig();

    @Override
    public void onUse() {
        use();
    }

    @Override
    public Key getKey() {
        return key;
    }

}
