package io.github.apace100.apoli.registry;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.ItemActionType;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.*;
import io.github.apace100.apoli.data.container.ContainerType;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.modifier.IModifierOperation;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class ApoliRegistryKeys {

    public static final RegistryKey<Registry<PowerConfiguration<PowerType>>> POWER_TYPE = create("power_type");

    public static final RegistryKey<Registry<ConditionConfiguration<BiEntityConditionType>>> BIENTITY_CONDITION_TYPE = create("bientity_condition_type");
    public static final RegistryKey<Registry<ConditionConfiguration<BiomeConditionType>>> BIOME_CONDITION_TYPE = create("biome_condition_type");
    public static final RegistryKey<Registry<ConditionConfiguration<BlockConditionType>>> BLOCK_CONDITION_TYPE = create("block_condition_type");
    public static final RegistryKey<Registry<ConditionConfiguration<DamageConditionType>>> DAMAGE_CONDITION_TYPE = create("damage_condition_type");
    public static final RegistryKey<Registry<ConditionConfiguration<EntityConditionType>>> ENTITY_CONDITION_TYPE = create("entity_condition_type");
    public static final RegistryKey<Registry<ConditionConfiguration<FluidConditionType>>> FLUID_CONDITION_TYPE = create("fluid_condition_type");
    public static final RegistryKey<Registry<ConditionConfiguration<ItemConditionType>>> ITEM_CONDITION_TYPE = create("item_condition_type");

    public static final RegistryKey<Registry<ActionConfiguration<BiEntityActionType>>> BIENTITY_ACTION_TYPE = create("bientity_action_type");
    public static final RegistryKey<Registry<ActionConfiguration<BlockActionType>>> BLOCK_ACTION_TYPE = create("block_action_type");
    public static final RegistryKey<Registry<ActionConfiguration<EntityActionType>>> ENTITY_ACTION_TYPE = create("entity_action_type");
    public static final RegistryKey<Registry<ActionConfiguration<ItemActionType>>> ITEM_ACTION_TYPE = create("item_action_type");

    public static final RegistryKey<Registry<IModifierOperation>> MODIFIER_OPERATION = create("modifier_operation");
    public static final RegistryKey<Registry<ContainerType>> CONTAINER_TYPE = create("container_type");

    private static <T> RegistryKey<Registry<T>> create(String path) {
        return RegistryKey.ofRegistry(Apoli.identifier(path));
    }

}
