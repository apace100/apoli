package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.ToastViewer;
import io.github.apace100.apoli.data.DynamicToastData;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class ShowToastAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity instanceof ToastViewer viewer) {
            viewer.apoli$showToast(data.get("toast"));
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("show_toast"),
            new SerializableData()
                .add("toast", DynamicToastData.DATA_TYPE),
            ShowToastAction::action
        );
    }

}
