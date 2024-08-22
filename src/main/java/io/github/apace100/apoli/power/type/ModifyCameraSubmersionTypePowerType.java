package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.entity.LivingEntity;

public class ModifyCameraSubmersionTypePowerType extends PowerType {

    private final CameraSubmersionType from;
    private final CameraSubmersionType to;

    public ModifyCameraSubmersionTypePowerType(Power power, LivingEntity entity, CameraSubmersionType from, CameraSubmersionType to) {
        super(power, entity);
        this.from = from;
        this.to = to;
    }

    public boolean doesModify(CameraSubmersionType original) {
        return from == null || from == original;
    }

    public CameraSubmersionType getNewType() {
        return to;
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_camera_submersion"),
            new SerializableData()
                .add("from", SerializableDataTypes.CAMERA_SUBMERSION_TYPE, null)
                .add("to", SerializableDataTypes.CAMERA_SUBMERSION_TYPE),
            data -> (power, entity) -> new ModifyCameraSubmersionTypePowerType(power, entity,
                data.get("from"),
                data.get("to")
            )
        ).allowCondition();
    }
}
