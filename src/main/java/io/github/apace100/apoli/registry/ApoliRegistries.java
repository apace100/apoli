package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.modifier.IModifierOperation;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
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

public class ApoliRegistries {

    public static final Registry<PowerFactory> POWER_FACTORY;
    public static final Registry<ConditionFactory<Entity>> ENTITY_CONDITION;
    public static final Registry<ConditionFactory<Pair<Entity, Entity>>> BIENTITY_CONDITION;
    public static final Registry<ConditionFactory<Pair<World, ItemStack>>> ITEM_CONDITION;
    public static final Registry<ConditionFactory<CachedBlockPosition>> BLOCK_CONDITION;
    public static final Registry<ConditionFactory<Pair<DamageSource, Float>>> DAMAGE_CONDITION;
    public static final Registry<ConditionFactory<FluidState>> FLUID_CONDITION;
    public static final Registry<ConditionFactory<RegistryEntry<Biome>>> BIOME_CONDITION;
    public static final Registry<ActionFactory<Entity>> ENTITY_ACTION;
    public static final Registry<ActionFactory<Pair<World, ItemStack>>> ITEM_ACTION;
    public static final Registry<ActionFactory<Triple<World, BlockPos, Direction>>> BLOCK_ACTION;
    public static final Registry<ActionFactory<Pair<Entity, Entity>>> BIENTITY_ACTION;
    public static final Registry<IModifierOperation> MODIFIER_OPERATION;

    static {

        POWER_FACTORY = create(ApoliRegistryKeys.POWER_FACTORY);

        ENTITY_CONDITION = create(ApoliRegistryKeys.ENTITY_CONDITION);
        BIENTITY_CONDITION = create(ApoliRegistryKeys.BIENTITY_CONDITION);
        ITEM_CONDITION = create(ApoliRegistryKeys.ITEM_CONDITION);
        BLOCK_CONDITION = create(ApoliRegistryKeys.BLOCK_CONDITION);
        DAMAGE_CONDITION = create(ApoliRegistryKeys.DAMAGE_CONDITION);
        FLUID_CONDITION = create(ApoliRegistryKeys.FLUID_CONDITION);
        BIOME_CONDITION = create(ApoliRegistryKeys.BIOME_CONDITION);

        ENTITY_ACTION = create(ApoliRegistryKeys.ENTITY_ACTION);
        ITEM_ACTION = create(ApoliRegistryKeys.ITEM_ACTION);
        BLOCK_ACTION = create(ApoliRegistryKeys.BLOCK_ACTION);
        BIENTITY_ACTION = create(ApoliRegistryKeys.BIENTITY_ACTION);

        MODIFIER_OPERATION = create(ApoliRegistryKeys.MODIFIER_OPERATION);

    }

    private static <T> Registry<T> create(RegistryKey<Registry<T>> registryKey) {
        return FabricRegistryBuilder.createSimple(registryKey).buildAndRegister();
    }

}
