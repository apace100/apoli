package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;

public class ModifyStatAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof ServerPlayerEntity serverPlayerEntity)) return;

        Stat<?> stat = data.get("stat");
        ServerStatHandler serverStatHandler = serverPlayerEntity.getStatHandler();

        int newValue;
        int originalValue = serverStatHandler.getStat(stat);

        serverPlayerEntity.resetStat(stat);

        Modifier modifier = data.get("modifier");
        newValue = (int)modifier.apply(entity, originalValue);

        serverPlayerEntity.increaseStat(stat, newValue);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("modify_stat"),
            new SerializableData()
                .add("stat", ApoliDataTypes.STAT)
                .add("modifier", Modifier.DATA_TYPE),
            ModifyStatAction::action
        );
    }
}
