package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.PotionUtil;

import java.util.LinkedList;
import java.util.List;

public class SpawnEffectCloudAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(entity.getWorld(), entity.getX(), entity.getY(), entity.getZ());
        if (entity instanceof LivingEntity) {
            areaEffectCloudEntity.setOwner((LivingEntity) entity);
        }
        areaEffectCloudEntity.setDuration(data.getInt("duration"));
        areaEffectCloudEntity.setDurationOnUse(data.getInt("duration_on_use"));
        areaEffectCloudEntity.setRadius(data.getFloat("radius"));
        areaEffectCloudEntity.setRadiusOnUse(data.getFloat("radius_on_use"));
        areaEffectCloudEntity.setWaitTime(data.getInt("wait_time"));
        areaEffectCloudEntity.setRadiusGrowth(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());
        List<StatusEffectInstance> effects = new LinkedList<>();
        if (data.isPresent("effect")) {
            effects.add(data.get("effect"));
        }
        if (data.isPresent("effects")) {
            effects.addAll(data.get("effects"));
        }
        areaEffectCloudEntity.setColor(PotionUtil.getColor(effects));
        effects.forEach(areaEffectCloudEntity::addEffect);

        entity.getWorld().spawnEntity(areaEffectCloudEntity);
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("spawn_effect_cloud"), new SerializableData()
                .add("radius", SerializableDataTypes.FLOAT, 3.0F)
                .add("radius_on_use", SerializableDataTypes.FLOAT, -0.5F)
                .add("duration", SerializableDataTypes.INT, 600)
                .add("duration_on_use", SerializableDataTypes.INT, 0)
                .add("wait_time", SerializableDataTypes.INT, 10)
                .add("effect", SerializableDataTypes.STATUS_EFFECT_INSTANCE, null)
                .add("effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES, null),
                SpawnEffectCloudAction::action);
    }

}
