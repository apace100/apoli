package io.github.apace100.apoli.power;

import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;

public class ActionOnBlockPlacePower extends BlockPlacePower {

    public ActionOnBlockPlacePower(PowerType<?> powerType, LivingEntity livingEntity, SerializableData.Instance data) {
        super(powerType, livingEntity, data);
    }

}
