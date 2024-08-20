package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.modifier.IModifierOperation;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
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

public class ApoliRegistries {

    public static final Registry<PowerTypeFactory<? extends PowerType>> POWER_FACTORY = create(ApoliRegistryKeys.POWER_FACTORY);

    public static final Registry<ConditionTypeFactory<Entity>> ENTITY_CONDITION = create(ApoliRegistryKeys.ENTITY_CONDITION);
    public static final Registry<ConditionTypeFactory<Pair<Entity, Entity>>> BIENTITY_CONDITION = create(ApoliRegistryKeys.BIENTITY_CONDITION);
    public static final Registry<ConditionTypeFactory<Pair<World, ItemStack>>> ITEM_CONDITION = create(ApoliRegistryKeys.ITEM_CONDITION);
    public static final Registry<ConditionTypeFactory<CachedBlockPosition>> BLOCK_CONDITION = create(ApoliRegistryKeys.BLOCK_CONDITION);
    public static final Registry<ConditionTypeFactory<Pair<DamageSource, Float>>> DAMAGE_CONDITION = create(ApoliRegistryKeys.DAMAGE_CONDITION);
    public static final Registry<ConditionTypeFactory<FluidState>> FLUID_CONDITION = create(ApoliRegistryKeys.FLUID_CONDITION);
    public static final Registry<ConditionTypeFactory<Pair<BlockPos, RegistryEntry<Biome>>>> BIOME_CONDITION = create(ApoliRegistryKeys.BIOME_CONDITION);

    public static final Registry<ActionTypeFactory<Entity>> ENTITY_ACTION = create(ApoliRegistryKeys.ENTITY_ACTION);
    public static final Registry<ActionTypeFactory<Pair<World, StackReference>>> ITEM_ACTION = create(ApoliRegistryKeys.ITEM_ACTION);
    public static final Registry<ActionTypeFactory<Triple<World, BlockPos, Direction>>> BLOCK_ACTION = create(ApoliRegistryKeys.BLOCK_ACTION);
    public static final Registry<ActionTypeFactory<Pair<Entity, Entity>>> BIENTITY_ACTION = create(ApoliRegistryKeys.BIENTITY_ACTION);

    public static final Registry<IModifierOperation> MODIFIER_OPERATION = create(ApoliRegistryKeys.MODIFIER_OPERATION);

    private static <T> Registry<T> create(RegistryKey<Registry<T>> registryKey) {
        return FabricRegistryBuilder.createSimple(registryKey).buildAndRegister();
    }

}
