package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;

public class ModifyStatEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ModifyStatEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("stat", SerializableDataTypes.STAT)
            .add("modifier", Modifier.DATA_TYPE),
        data -> new ModifyStatEntityActionType(
            data.get("stat"),
            data.get("modifier")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("stat", actionType.stat)
            .set("modifier", actionType.modifier)
    );

    private final Stat<?> stat;
    private final Modifier modifier;

    public ModifyStatEntityActionType(Stat<?> stat, Modifier modifier) {
        this.stat = stat;
        this.modifier = modifier;
    }

    @Override
    protected void execute(Entity entity) {

        if (entity instanceof ServerPlayerEntity serverPlayer) {

            ServerStatHandler statHandler = serverPlayer.getStatHandler();
            int originalValue = statHandler.getStat(stat);

            serverPlayer.resetStat(stat);
            serverPlayer.increaseStat(stat, (int) modifier.apply(entity, originalValue));

        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.MODIFY_STAT;
    }

}
