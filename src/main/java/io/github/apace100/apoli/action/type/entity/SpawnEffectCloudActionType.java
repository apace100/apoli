package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;

public class SpawnEffectCloudActionType {

    public static void action(Entity entity, PotionContentsComponent potionContents, float radius, float radiusOnUse, int duration, int durationOnUse, int waitTime) {

        if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        AreaEffectCloudEntity aec = new AreaEffectCloudEntity(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ());
        if (entity instanceof LivingEntity living) {
            aec.setOwner(living);
        }

        aec.setPotionContents(potionContents);
        aec.setRadius(radius);
        aec.setRadiusOnUse(radiusOnUse);
        aec.setDuration(duration);
        aec.setDurationOnUse(durationOnUse);
        aec.setWaitTime(waitTime);

        serverWorld.spawnEntity(aec);

    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("spawn_effect_cloud"),
            new SerializableData()
                .add("effect_component", SerializableDataTypes.POTION_CONTENTS_COMPONENT, PotionContentsComponent.DEFAULT)
                .add("radius", SerializableDataTypes.FLOAT, 3.0F)
                .add("radius_on_use", SerializableDataTypes.FLOAT, -0.5F)
                .add("duration", SerializableDataTypes.INT, 600)
                .add("duration_on_use", SerializableDataTypes.INT, 0)
                .add("wait_time", SerializableDataTypes.INT, 10),
            (data, entity) -> action(entity,
                data.get("effect_component"),
                data.get("radius"),
                data.get("radius_on_use"),
                data.get("duration"),
                data.get("duration_on_use"),
                data.get("wait_time")
            )
        );
    }

}
