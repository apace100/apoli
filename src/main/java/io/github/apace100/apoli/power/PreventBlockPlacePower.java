package io.github.apace100.apoli.power;

import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;

public class PreventBlockPlacePower extends BlockPlacePower {

    public PreventBlockPlacePower(PowerType<?> powerType, LivingEntity livingEntity, SerializableData.Instance data) {
        super(powerType, livingEntity, data);
    }

}
