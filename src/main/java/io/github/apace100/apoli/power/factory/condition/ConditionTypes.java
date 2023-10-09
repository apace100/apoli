package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class ConditionTypes {

    public static ConditionType<Entity> ENTITY = new ConditionType<>("Entity Condition", ApoliRegistries.ENTITY_CONDITION);
    public static ConditionType<Pair<Entity, Entity>> BIENTITY = new ConditionType<>("Bi-Entity Condition", ApoliRegistries.BIENTITY_CONDITION);
    public static ConditionType<Pair<World, ItemStack>> ITEM = new ConditionType<>("Item Condition", ApoliRegistries.ITEM_CONDITION);
    public static ConditionType<CachedBlockPosition> BLOCK = new ConditionType<>("Block Condition", ApoliRegistries.BLOCK_CONDITION);
    public static ConditionType<Pair<DamageSource, Float>> DAMAGE = new ConditionType<>("Damage Condition", ApoliRegistries.DAMAGE_CONDITION);
    public static ConditionType<FluidState> FLUID = new ConditionType<>("Fluid Condition", ApoliRegistries.FLUID_CONDITION);
    public static ConditionType<RegistryEntry<Biome>> BIOME = new ConditionType<>("Biome Condition", ApoliRegistries.BIOME_CONDITION);

}
