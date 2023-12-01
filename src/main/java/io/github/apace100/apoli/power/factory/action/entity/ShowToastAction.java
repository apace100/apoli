package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.CustomToastViewer;
import io.github.apace100.apoli.data.CustomToastData;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;

public class ShowToastAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        if (entity instanceof CustomToastViewer viewer) {
            viewer.apoli$showToast(data.get("toast"));
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("show_toast"),
            new SerializableData()
                .add("toast", CustomToastData.DATA_TYPE),
            ShowToastAction::action
        );
    }

}
