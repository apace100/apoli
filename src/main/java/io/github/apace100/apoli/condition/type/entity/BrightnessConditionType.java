package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 *  TODO: Re-calculate the ambient darkness in {@link net.minecraft.client.world.ClientWorld} (via a mixin) instead of here -eggohito
 */
public class BrightnessConditionType {

    public static boolean condition(Entity entity, Comparison comparison, float compareTo) {

        World world = entity.getWorld();
        if (world.isClient) {
            world.calculateAmbientDarkness();
        }

        BlockPos pos = BlockPos.ofFloored(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
        float brightness = world.getBrightness(pos);

        return comparison.compare(brightness, compareTo);

    }

    public static ConditionTypeFactory<Entity> getFactory() {
        return new ConditionTypeFactory<>(
            Apoli.identifier("brightness"),
            new SerializableData()
                .add("comparison", ApoliDataTypes.COMPARISON)
                .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> condition(entity,
                data.get("comparison"),
                data.get("compare_to")
            )
        );
    }

}
