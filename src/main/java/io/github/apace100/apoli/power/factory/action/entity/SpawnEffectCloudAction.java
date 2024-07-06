package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class SpawnEffectCloudAction {

    public static void action(SerializableData.Instance data, Entity entity) {

        AreaEffectCloudEntity aec = new AreaEffectCloudEntity(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ());
        if (entity instanceof LivingEntity livingEntity) {
            aec.setOwner(livingEntity);
        }

        aec.setRadius(data.getFloat("radius"));
        aec.setRadiusOnUse(data.getFloat("radius_on_use"));
        aec.setDuration(data.getInt("duration"));
        aec.setDurationOnUse(data.getInt("duration_on_use"));
        aec.setWaitTime(data.getInt("wait_time"));
        aec.setPotionContents(data.get("effect_component"));

        entity.getWorld().spawnEntity(aec);

    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("spawn_effect_cloud"),
            new SerializableData()
                .add("radius", SerializableDataTypes.FLOAT, 3.0F)
                .add("radius_on_use", SerializableDataTypes.FLOAT, -0.5F)
                .add("duration", SerializableDataTypes.INT, 600)
                .add("duration_on_use", SerializableDataTypes.INT, 0)
                .add("wait_time", SerializableDataTypes.INT, 10)
                .add("effect_component", SerializableDataTypes.POTION_CONTENTS_COMPONENT, PotionContentsComponent.DEFAULT),
            SpawnEffectCloudAction::action
        );
    }

}
