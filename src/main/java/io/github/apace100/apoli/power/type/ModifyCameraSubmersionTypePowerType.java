package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.enums.CameraSubmersionType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ModifyCameraSubmersionTypePowerType extends PowerType {

    public static final TypedDataObjectFactory<ModifyCameraSubmersionTypePowerType> DATA_FACTORY = createConditionedDataFactory(
        new SerializableData()
            .add("from", SerializableDataTypes.CAMERA_SUBMERSION_TYPE.optional(), Optional.empty())
            .add("to", SerializableDataTypes.CAMERA_SUBMERSION_TYPE),
        (data, condition) -> new ModifyCameraSubmersionTypePowerType(
            data.get("from"),
            data.get("to"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("from", powerType.from)
            .set("to", powerType.to)
    );

    private final Optional<CameraSubmersionType> from;
    private final CameraSubmersionType to;

    public ModifyCameraSubmersionTypePowerType(Optional<CameraSubmersionType> from, CameraSubmersionType to, Optional<EntityCondition> condition) {
        super(condition);
        this.from = from;
        this.to = to;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_CAMERA_SUBMERSION;
    }

    public boolean doesModify(CameraSubmersionType original) {
        return from.map(from -> from == original).orElse(true);
    }

    public CameraSubmersionType getNewType() {
        return to;
    }

}
