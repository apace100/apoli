package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class ModifyDeathTicksEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<ModifyDeathTicksEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("modifier", Modifier.DATA_TYPE),
        data -> new ModifyDeathTicksEntityActionType(
            data.get("modifier")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("modifier", actionType.modifier)
    );

    private final Modifier modifier;

    public ModifyDeathTicksEntityActionType(Modifier modifier) {
        this.modifier = modifier;
    }

    @Override
    protected void execute(Entity entity) {

        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.deathTime = (int) modifier.apply(entity, livingEntity.deathTime);
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.MODIFY_DEATH_TICKS;
    }

}
