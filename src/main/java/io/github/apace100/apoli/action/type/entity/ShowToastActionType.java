package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.CustomToastViewer;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.CustomToastData;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.util.dynamic.NullOps;

public class ShowToastActionType {

    public static void action(SerializableData.Instance data, Entity entity) {

        if (!entity.getWorld().isClient && entity instanceof CustomToastViewer viewer) {
            //  Since custom toast data doesn't use the dynamic ops when being converted to serializable data instance,
            //  using any dynamic ops is fine
            viewer.apoli$showToast(CustomToastData.DATA_TYPE.fromData(NullOps.INSTANCE, data));
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("show_toast"),
            CustomToastData.DATA_TYPE.serializableData(),
            ShowToastActionType::action
        );
    }

}
