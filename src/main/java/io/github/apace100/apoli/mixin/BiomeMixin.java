package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.access.BiomeWeatherAccess;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Biome.class)
public class BiomeMixin implements BiomeWeatherAccess {

    @Unique
    private float apoli$downfall;

    @Override
    public float getDownfall() {
        return apoli$downfall;
    }

    @Override
    public void setDownfall(float downfall) {
        apoli$downfall = downfall;
    }
}
