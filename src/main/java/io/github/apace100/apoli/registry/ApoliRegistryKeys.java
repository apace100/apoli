package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.condition.type.*;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.type.PowerType;
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

    public static final RegistryKey<Registry<PowerTypeFactory<? extends PowerType>>> POWER_FACTORY = create("power_factory");

    public static final RegistryKey<Registry<ConditionTypeFactory<Entity>>> ENTITY_CONDITION = create("entity_condition");
    public static final RegistryKey<Registry<ConditionTypeFactory<Pair<Entity, Entity>>>> BIENTITY_CONDITION = create("bientity_condition");
    public static final RegistryKey<Registry<ConditionTypeFactory<Pair<World, ItemStack>>>> ITEM_CONDITION = create("item_condition");
    public static final RegistryKey<Registry<ConditionTypeFactory<CachedBlockPosition>>> BLOCK_CONDITION = create("block_condition");
    public static final RegistryKey<Registry<ConditionTypeFactory<Pair<DamageSource, Float>>>> DAMAGE_CONDITION = create("damage_condition");
    public static final RegistryKey<Registry<ConditionTypeFactory<FluidState>>> FLUID_CONDITION = create("fluid_condition");
    public static final RegistryKey<Registry<ConditionTypeFactory<Pair<BlockPos, RegistryEntry<Biome>>>>> BIOME_CONDITION = create("biome_condition");

    public static final RegistryKey<Registry<ConditionConfiguration<BiEntityConditionType>>> BIENTITY_CONDITION_TYPE = create("bientity_condition_type");
    public static final RegistryKey<Registry<ConditionConfiguration<BiomeConditionType>>> BIOME_CONDITION_TYPE = create("biome_condition_type");
    public static final RegistryKey<Registry<ConditionConfiguration<BlockConditionType>>> BLOCK_CONDITION_TYPE = create("block_condition_type");
    public static final RegistryKey<Registry<ConditionConfiguration<DamageConditionType>>> DAMAGE_CONDITION_TYPE = create("damage_condition_type");
    public static final RegistryKey<Registry<ConditionConfiguration<EntityConditionType>>> ENTITY_CONDITION_TYPE = create("entity_condition_type");
    public static final RegistryKey<Registry<ConditionConfiguration<FluidConditionType>>> FLUID_CONDITION_TYPE = create("fluid_condition_type");
    public static final RegistryKey<Registry<ConditionConfiguration<ItemConditionType>>> ITEM_CONDITION_TYPE = create("item_condition_type");

    public static final RegistryKey<Registry<ActionTypeFactory<Entity>>> ENTITY_ACTION = create("entity_action");
    public static final RegistryKey<Registry<ActionTypeFactory<Pair<World, StackReference>>>> ITEM_ACTION = create("item_action");
    public static final RegistryKey<Registry<ActionTypeFactory<Triple<World, BlockPos, Direction>>>> BLOCK_ACTION = create("block_action");
    public static final RegistryKey<Registry<ActionTypeFactory<Pair<Entity, Entity>>>> BIENTITY_ACTION = create("bientity_action");

    public static final RegistryKey<Registry<ActionConfiguration<BiEntityActionType>>> BIENTITY_ACTION_TYPE = create("bientity_action_type");
    public static final RegistryKey<Registry<ActionConfiguration<BlockActionType>>> BLOCK_ACTION_TYPE = create("block_action_type");
    public static final RegistryKey<Registry<ActionConfiguration<EntityActionType>>> ENTITY_ACTION_TYPE = create("entity_action_type");
    public static final RegistryKey<Registry<ActionConfiguration<ItemActionType>>> ITEM_ACTION_TYPE = create("item_action_type");

    public static final RegistryKey<Registry<IModifierOperation>> MODIFIER_OPERATION = create("modifier_operation");

    private static <T> RegistryKey<Registry<T>> create(String path) {
        return RegistryKey.ofRegistry(Apoli.identifier(path));
    }

}
