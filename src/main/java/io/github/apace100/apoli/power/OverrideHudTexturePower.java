package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class OverrideHudTexturePower extends Power {
    private final Identifier statusBarTexture;
    public OverrideHudTexturePower(PowerType<?> type, LivingEntity entity, Identifier statusBarTexture) {
        super(type, entity);
        this.statusBarTexture = statusBarTexture;
    }

    public Identifier getStatusBarTexture() {
        return statusBarTexture;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("status_bar_texture"),
            new SerializableData()
                .add("texture", SerializableDataTypes.IDENTIFIER, null),
            data ->
                (type, player) ->
                    new OverrideHudTexturePower(type, player, data.getId("texture")))
            .allowCondition();
    }
}