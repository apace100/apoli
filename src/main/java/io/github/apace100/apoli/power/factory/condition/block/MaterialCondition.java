package io.github.apace100.apoli.power.factory.condition.block;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.LegacyMaterial;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;

import java.util.List;

//  TODO: Perhaps this should be removed? It's been long since its deprecation...
public class MaterialCondition {

    public static boolean condition(SerializableData.Instance data, CachedBlockPosition cachedBlockPosition) {
        BlockState blockState = cachedBlockPosition.getBlockState();
        if(data.isPresent("material")) {
            if(data.<LegacyMaterial>get("material").blockStateIsOfMaterial(blockState)) {
                return true;
            }
        }
        if(data.isPresent("materials")) {
            return data.<List<LegacyMaterial>>get("materials").stream().anyMatch(m -> m.blockStateIsOfMaterial(blockState));
        }
        return false;
    }

    public static ConditionFactory<CachedBlockPosition> getFactory() {
        return new ConditionFactory<>(Apoli.identifier("material"),
            new SerializableData()
                .add("material", ApoliDataTypes.LEGACY_MATERIAL, null)
                .add("materials", ApoliDataTypes.LEGACY_MATERIALS, null),
            MaterialCondition::condition
        );
    }
}
