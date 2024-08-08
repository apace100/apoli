package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class ElytraFlightPowerType extends PowerType {

    private final boolean renderElytra;
    private final Identifier textureLocation;

    public ElytraFlightPowerType(Power power, LivingEntity entity, boolean renderElytra, Identifier textureLocation) {
        super(power, entity);
        this.renderElytra = renderElytra;
        this.textureLocation = textureLocation;
    }

    public boolean shouldRenderElytra() {
        return renderElytra;
    }

    public Identifier getTextureLocation() {
        return textureLocation;
    }

    public static PowerTypeFactory<ElytraFlightPowerType> getFactory() {

        //  TODO: Manually do vanilla elytra flight stuff using the API -eggohito
        EntityElytraEvents.CUSTOM.register((entity, tickElytra) -> PowerHolderComponent.hasPowerType(entity, ElytraFlightPowerType.class));

        return new PowerTypeFactory<>(Apoli.identifier("elytra_flight"),
            new SerializableData()
                .add("render_elytra", SerializableDataTypes.BOOLEAN)
                .add("texture_location", SerializableDataTypes.IDENTIFIER, null),
            data -> (power, entity) -> new ElytraFlightPowerType(power, entity,
                data.getBoolean("render_elytra"),
                data.getId("texture_location")
            )
        ).allowCondition();

    }

}
