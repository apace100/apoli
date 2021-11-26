package io.github.apace100.apoli.power.factory.condition.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.Material;
import net.minecraft.block.pattern.CachedBlockPosition;

import java.util.List;

public class MaterialCondition {

    public static boolean condition(SerializableData.Instance data, CachedBlockPosition cachedBlockPosition) {
        Material material = cachedBlockPosition.getBlockState().getMaterial();
        if(data.isPresent("material")) {
            if(material == data.get("material")) {
                return true;
            }
        }
        if(data.isPresent("materials")) {
            return data.<List<Material>>get("materials").stream().anyMatch(m -> m == material);
        }
        return false;
    }

    public static ConditionFactory<CachedBlockPosition> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("material"),
            new SerializableData()
                .add("material", SerializableDataTypes.MATERIAL, null)
                .add("materials", SerializableDataTypes.MATERIALS, null),
            MaterialCondition::condition
        );
    }
}
