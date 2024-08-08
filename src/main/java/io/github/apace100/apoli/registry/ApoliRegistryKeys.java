package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.modifier.IModifierOperation;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Triple;

public class ApoliRegistryKeys {

    public static final RegistryKey<Registry<PowerFactory<? extends Power>>> POWER_FACTORY = create("power_factory");

    public static final RegistryKey<Registry<ConditionFactory<Entity>>> ENTITY_CONDITION = create("entity_condition");
    public static final RegistryKey<Registry<ConditionFactory<Pair<Entity, Entity>>>> BIENTITY_CONDITION = create("bientity_condition");
    public static final RegistryKey<Registry<ConditionFactory<Pair<World, ItemStack>>>> ITEM_CONDITION = create("item_condition");
    public static final RegistryKey<Registry<ConditionFactory<CachedBlockPosition>>> BLOCK_CONDITION = create("block_condition");
    public static final RegistryKey<Registry<ConditionFactory<Pair<DamageSource, Float>>>> DAMAGE_CONDITION = create("damage_condition");
    public static final RegistryKey<Registry<ConditionFactory<FluidState>>> FLUID_CONDITION = create("fluid_condition");
    public static final RegistryKey<Registry<ConditionFactory<RegistryEntry<Biome>>>> BIOME_CONDITION = create("biome_condition");

    public static final RegistryKey<Registry<ActionFactory<Entity>>> ENTITY_ACTION = create("entity_action");
    public static final RegistryKey<Registry<ActionFactory<Pair<World, StackReference>>>> ITEM_ACTION = create("item_action");
    public static final RegistryKey<Registry<ActionFactory<Triple<World, BlockPos, Direction>>>> BLOCK_ACTION = create("block_action");
    public static final RegistryKey<Registry<ActionFactory<Pair<Entity, Entity>>>> BIENTITY_ACTION = create("bientity_action");

    public static final RegistryKey<Registry<IModifierOperation>> MODIFIER_OPERATION = create("modifier_operation");

    private static <T> RegistryKey<Registry<T>> create(String path) {
        return RegistryKey.ofRegistry(Apoli.identifier(path));
    }

}
