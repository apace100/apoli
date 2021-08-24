package io.github.apace100.apoli.power;

import net.minecraft.entity.LivingEntity;

public class SelfGlowPower extends Power {

    private final boolean useTeams;
    private final float red;
    private final float green;
    private final float blue;

    public SelfGlowPower(PowerType<?> type, LivingEntity entity, boolean useTeams, float red, float green, float blue) {
        super(type, entity);
        this.useTeams = useTeams;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public boolean usesTeams() {
        return useTeams;
    }

    public float getRed() {
        return red;
    }

    public float getGreen() {
        return green;
    }

    public float getBlue() {
        return blue;
    }
}
