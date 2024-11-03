package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ElytraFlightPowerType extends PowerType {

    public static final TypedDataObjectFactory<ElytraFlightPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("texture_location", SerializableDataTypes.IDENTIFIER.optional(), Optional.empty())
            .add("render_elytra", SerializableDataTypes.BOOLEAN),
        (data, condition) -> new ElytraFlightPowerType(
            data.get("texture_location"),
            data.get("render_elytra"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("texture_location", powerType.textureLocation)
            .set("render_elytra", powerType.renderElytra)
    );

    private final Optional<Identifier> textureLocation;
    private final boolean renderElytra;

    public ElytraFlightPowerType(Optional<Identifier> textureLocation, boolean renderElytra,Optional<EntityCondition> condition) {
        super(condition);
        this.textureLocation = textureLocation;
        this.renderElytra = renderElytra;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ELYTRA_FLIGHT;
    }

    @Override
    public boolean isActive() {
        return getTextureLocation().isPresent() && super.isActive();
    }

    public Optional<Identifier> getTextureLocation() {
        return textureLocation;
    }

    public boolean shouldRenderElytra() {
        return renderElytra;
    }

    //  TODO: Manually do vanilla elytra flight stuff using the API -eggohito
    public static boolean integrateCustomCallback(LivingEntity entity, boolean tickElytra) {
        return PowerHolderComponent.hasPowerType(entity, ElytraFlightPowerType.class);
    }

}
