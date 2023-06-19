package io.github.apace100.apoli.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.SlimeBlock;

import java.util.HashMap;
import java.util.Map;

public class BouncinessMultiplierRegistry {

    private static final Map<Class<? extends AbstractBlock>, Double> BOUNCINESS_MULTIPLIERS = new HashMap<>();

    public static void registerAll() {
        BOUNCINESS_MULTIPLIERS.put(BedBlock.class, 0.66);
        BOUNCINESS_MULTIPLIERS.put(SlimeBlock.class, 1.0);
    }

    public static <T extends AbstractBlock> double getValue(Class<T> key) {
        if (BOUNCINESS_MULTIPLIERS.containsKey(key)) {
            return BOUNCINESS_MULTIPLIERS.get(key);
        }
        return 0.0F;
    }

    public static <T extends AbstractBlock> void register(Class<T> blockClass, double value) {
        BOUNCINESS_MULTIPLIERS.put(blockClass, value);
    }
}
