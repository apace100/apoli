package io.github.apace100.apoli.power;

import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.entity.LivingEntity;

import java.util.Optional;

public class ModifyCameraSubmersionTypePower extends Power {

    private final Optional<CameraSubmersionType> from;
    private final CameraSubmersionType to;

    public ModifyCameraSubmersionTypePower(PowerType<?> type, LivingEntity entity, Optional<CameraSubmersionType> from, CameraSubmersionType to) {
        super(type, entity);
        this.from = from;
        this.to = to;
    }

    public boolean doesModify(CameraSubmersionType original) {
        return from.isEmpty() || from.get() == original;
    }

    public CameraSubmersionType getNewType() {
        return to;
    }
}
