package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.ResourceOperation;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class ChangeInsomniaTicksAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (!(entity instanceof ServerPlayerEntity serverPlayerEntity)) return;

        Stat<Identifier> stat = Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST);
        ServerStatHandler serverStatHandler = serverPlayerEntity.getStatHandler();
        ResourceOperation operation = data.get("operation");

        int newValue = 0;
        int change = data.getInt("change");
        int originalValue = serverStatHandler.getStat(stat);

        serverPlayerEntity.resetStat(stat);

        if (operation == ResourceOperation.ADD) newValue = MathHelper.clamp(change, 0, Integer.MAX_VALUE);
        else if (operation == ResourceOperation.SET) newValue = MathHelper.clamp(originalValue + change, 0, Integer.MAX_VALUE);

        serverPlayerEntity.increaseStat(stat, newValue);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("change_insomnia_ticks"),
            new SerializableData()
                .add("change", SerializableDataTypes.INT)
                .add("operation", ApoliDataTypes.RESOURCE_OPERATION, ResourceOperation.ADD),
            ChangeInsomniaTicksAction::action
        );
    }
}
