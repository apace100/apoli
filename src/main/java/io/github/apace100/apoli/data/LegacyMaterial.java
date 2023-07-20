package io.github.apace100.apoli.data;

import io.github.apace100.apoli.Apoli;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class LegacyMaterial {
    private final TagKey<Block> materialTagKey;
    private final String material;

    public LegacyMaterial(String material) {
        materialTagKey = TagKey.of(RegistryKeys.BLOCK, Apoli.identifier("material/" + material));
        this.material = material;
    }
    public String getMaterial() {
        return this.material;
    }

    public boolean blockStateIsOfMaterial(BlockState blockState) {
        return blockState.isIn(materialTagKey);
    }
}
