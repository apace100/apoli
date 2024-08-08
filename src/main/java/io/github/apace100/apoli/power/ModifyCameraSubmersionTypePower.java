package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.entity.LivingEntity;

import java.util.Optional;

public class ModifyCameraSubmersionTypePower extends Power {

    private final Optional<CameraSubmersionType> from;
    private final CameraSubmersionType to;

    public ModifyCameraSubmersionTypePower(PowerType type, LivingEntity entity, Optional<CameraSubmersionType> from, CameraSubmersionType to) {
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

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_camera_submersion"),
            new SerializableData()
                .add("from", SerializableDataTypes.CAMERA_SUBMERSION_TYPE, null)
                .add("to", SerializableDataTypes.CAMERA_SUBMERSION_TYPE),
            data ->
                (type, player) -> new ModifyCameraSubmersionTypePower(type, player,
                    data.isPresent("from") ? Optional.of((CameraSubmersionType)data.get("from")) : Optional.empty(),
                    (CameraSubmersionType)data.get("to")))
            .allowCondition();
    }
}
