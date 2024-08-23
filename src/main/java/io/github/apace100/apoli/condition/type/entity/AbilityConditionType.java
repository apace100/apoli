package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AbilityConditionType {

    public static boolean condition(Entity entity, PlayerAbility ability) {
        return !entity.getWorld().isClient
            && entity instanceof PlayerEntity player
            && ability.isEnabledFor(player);
    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("ability"),
            new SerializableData()
                .add("ability", ApoliDataTypes.PLAYER_ABILITY),
            (data, entity) -> condition(entity,
                data.get("ability")
            )
        );
    }

}
