package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.world.biome.Biome;

public class ConditionTypes {

    public static ConditionType<Entity> ENTITY = new ConditionType<>("EntityCondition", ApoliRegistries.ENTITY_CONDITION);
    public static ConditionType<Pair<Entity, Entity>> BIENTITY = new ConditionType<>("BiEntityCondition", ApoliRegistries.BIENTITY_CONDITION);
    public static ConditionType<ItemStack> ITEM = new ConditionType<>("ItemCondition", ApoliRegistries.ITEM_CONDITION);
    public static ConditionType<CachedBlockPosition> BLOCK = new ConditionType<>("BlockCondition", ApoliRegistries.BLOCK_CONDITION);
    public static ConditionType<Pair<DamageSource, Float>> DAMAGE = new ConditionType<>("DamageCondition", ApoliRegistries.DAMAGE_CONDITION);
    public static ConditionType<FluidState> FLUID = new ConditionType<>("FluidCondition", ApoliRegistries.FLUID_CONDITION);
    public static ConditionType<Biome> BIOME = new ConditionType<>("BiomeCondition", ApoliRegistries.BIOME_CONDITION);

}
