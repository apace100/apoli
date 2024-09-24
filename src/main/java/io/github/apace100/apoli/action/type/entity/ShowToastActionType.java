package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.CustomToastViewer;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.data.CustomToastData;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class ShowToastActionType {

    public static void action(SerializableData.Instance data, Entity entity) {

        if (!entity.getWorld().isClient && entity instanceof CustomToastViewer viewer) {
            viewer.apoli$showToast(CustomToastData.FACTORY.fromData(data));
        }

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("show_toast"),
            CustomToastData.FACTORY.getSerializableData(),
            ShowToastActionType::action
        );
    }

}
