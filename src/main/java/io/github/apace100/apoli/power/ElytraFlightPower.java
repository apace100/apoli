package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class ElytraFlightPower extends Power {

    private final boolean renderElytra;
    private final Identifier textureLocation;

    public ElytraFlightPower(PowerType type, LivingEntity entity, boolean renderElytra, Identifier textureLocation) {
        super(type, entity);
        this.renderElytra = renderElytra;
        this.textureLocation = textureLocation;
    }

    public boolean shouldRenderElytra() {
        return renderElytra;
    }

    public Identifier getTextureLocation() {
        return textureLocation;
    }

    public static PowerFactory createFactory() {
        EntityElytraEvents.CUSTOM.register((livingEntity, b) ->
            PowerHolderComponent.hasPower(livingEntity, ElytraFlightPower.class));

        return new PowerFactory<>(Apoli.identifier("elytra_flight"),
            new SerializableData()
                .add("render_elytra", SerializableDataTypes.BOOLEAN)
                .add("texture_location", SerializableDataTypes.IDENTIFIER, null),
            data ->
                (type, player) -> new ElytraFlightPower(type, player, data.getBoolean("render_elytra"), data.getId("texture_location")))
            .allowCondition();
    }
}
