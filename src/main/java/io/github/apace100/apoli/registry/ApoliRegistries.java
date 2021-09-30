package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.ClassUtil;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Triple;

public class ApoliRegistries {

    public static final Registry<PowerFactory> POWER_FACTORY;
    public static final Registry<ConditionFactory<Entity>> ENTITY_CONDITION;
    public static final Registry<ConditionFactory<Pair<Entity, Entity>>> BIENTITY_CONDITION;
    public static final Registry<ConditionFactory<ItemStack>> ITEM_CONDITION;
    public static final Registry<ConditionFactory<CachedBlockPosition>> BLOCK_CONDITION;
    public static final Registry<ConditionFactory<Pair<DamageSource, Float>>> DAMAGE_CONDITION;
    public static final Registry<ConditionFactory<FluidState>> FLUID_CONDITION;
    public static final Registry<ConditionFactory<Biome>> BIOME_CONDITION;
    public static final Registry<ActionFactory<Entity>> ENTITY_ACTION;
    public static final Registry<ActionFactory<Pair<World, ItemStack>>> ITEM_ACTION;
    public static final Registry<ActionFactory<Triple<World, BlockPos, Direction>>> BLOCK_ACTION;
    public static final Registry<ActionFactory<Pair<Entity, Entity>>> BIENTITY_ACTION;

    static {
        POWER_FACTORY = FabricRegistryBuilder.createSimple(PowerFactory.class, Apoli.identifier("power_factory")).buildAndRegister();
        ENTITY_CONDITION = FabricRegistryBuilder.createSimple(ClassUtil.<ConditionFactory<Entity>>castClass(ConditionFactory.class), Apoli.identifier("entity_condition")).buildAndRegister();
        BIENTITY_CONDITION = FabricRegistryBuilder.createSimple(ClassUtil.<ConditionFactory<Pair<Entity, Entity>>>castClass(ConditionFactory.class), Apoli.identifier("bientity_condition")).buildAndRegister();
        ITEM_CONDITION = FabricRegistryBuilder.createSimple(ClassUtil.<ConditionFactory<ItemStack>>castClass(ConditionFactory.class), Apoli.identifier("item_condition")).buildAndRegister();
        BLOCK_CONDITION = FabricRegistryBuilder.createSimple(ClassUtil.<ConditionFactory<CachedBlockPosition>>castClass(ConditionFactory.class), Apoli.identifier("block_condition")).buildAndRegister();
        DAMAGE_CONDITION = FabricRegistryBuilder.createSimple(ClassUtil.<ConditionFactory<Pair<DamageSource, Float>>>castClass(ConditionFactory.class), Apoli.identifier("damage_condition")).buildAndRegister();
        FLUID_CONDITION = FabricRegistryBuilder.createSimple(ClassUtil.<ConditionFactory<FluidState>>castClass(ConditionFactory.class), Apoli.identifier("fluid_condition")).buildAndRegister();
        BIOME_CONDITION = FabricRegistryBuilder.createSimple(ClassUtil.<ConditionFactory<Biome>>castClass(ConditionFactory.class), Apoli.identifier("biome_condition")).buildAndRegister();
        ENTITY_ACTION = FabricRegistryBuilder.createSimple(ClassUtil.<ActionFactory<Entity>>castClass(ActionFactory.class), Apoli.identifier("entity_action")).buildAndRegister();
        ITEM_ACTION = FabricRegistryBuilder.createSimple(ClassUtil.<ActionFactory<Pair<World, ItemStack>>>castClass(ActionFactory.class), Apoli.identifier("item_action")).buildAndRegister();
        BLOCK_ACTION = FabricRegistryBuilder.createSimple(ClassUtil.<ActionFactory<Triple<World, BlockPos, Direction>>>castClass(ActionFactory.class), Apoli.identifier("block_action")).buildAndRegister();
        BIENTITY_ACTION = FabricRegistryBuilder.createSimple(ClassUtil.<ActionFactory<Pair<Entity, Entity>>>castClass(ActionFactory.class), Apoli.identifier("bientity_action")).buildAndRegister();
    }
}
