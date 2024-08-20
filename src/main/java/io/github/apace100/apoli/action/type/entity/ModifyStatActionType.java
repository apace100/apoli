package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;

public class ModifyStatActionType {

    public static void action(Entity entity, Stat<?> stat, Modifier modifier) {

        if (!(entity instanceof ServerPlayerEntity serverPlayerEntity)) {
            return;
        }

        ServerStatHandler serverStatHandler = serverPlayerEntity.getStatHandler();
        int originalValue = serverStatHandler.getStat(stat);

        serverPlayerEntity.resetStat(stat);
        serverPlayerEntity.increaseStat(stat, (int) modifier.apply(entity, originalValue));

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("modify_stat"),
            new SerializableData()
                .add("stat", SerializableDataTypes.STAT)
                .add("modifier", Modifier.DATA_TYPE),
            (data, entity) -> action(entity,
                data.get("stat"),
                data.get("modifier")
            )
        );
    }
}
