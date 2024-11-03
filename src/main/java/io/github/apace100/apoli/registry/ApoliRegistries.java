package io.github.apace100.apoli.registry;

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
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class ApoliRegistries {

    public static final Registry<PowerConfiguration<PowerType>> POWER_TYPE = create(ApoliRegistryKeys.POWER_TYPE);

    public static final Registry<ConditionConfiguration<BiEntityConditionType>> BIENTITY_CONDITION_TYPE = create(ApoliRegistryKeys.BIENTITY_CONDITION_TYPE);
    public static final Registry<ConditionConfiguration<BiomeConditionType>> BIOME_CONDITION_TYPE = create(ApoliRegistryKeys.BIOME_CONDITION_TYPE);
    public static final Registry<ConditionConfiguration<BlockConditionType>> BLOCK_CONDITION_TYPE = create(ApoliRegistryKeys.BLOCK_CONDITION_TYPE);
    public static final Registry<ConditionConfiguration<DamageConditionType>> DAMAGE_CONDITION_TYPE = create(ApoliRegistryKeys.DAMAGE_CONDITION_TYPE);
    public static final Registry<ConditionConfiguration<EntityConditionType>> ENTITY_CONDITION_TYPE = create(ApoliRegistryKeys.ENTITY_CONDITION_TYPE);
    public static final Registry<ConditionConfiguration<FluidConditionType>> FLUID_CONDITION_TYPE = create(ApoliRegistryKeys.FLUID_CONDITION_TYPE);
    public static final Registry<ConditionConfiguration<ItemConditionType>> ITEM_CONDITION_TYPE = create(ApoliRegistryKeys.ITEM_CONDITION_TYPE);

    public static final Registry<ActionConfiguration<BiEntityActionType>> BIENTITY_ACTION_TYPE = create(ApoliRegistryKeys.BIENTITY_ACTION_TYPE);
    public static final Registry<ActionConfiguration<BlockActionType>> BLOCK_ACTION_TYPE = create(ApoliRegistryKeys.BLOCK_ACTION_TYPE);
    public static final Registry<ActionConfiguration<EntityActionType>> ENTITY_ACTION_TYPE = create(ApoliRegistryKeys.ENTITY_ACTION_TYPE);
    public static final Registry<ActionConfiguration<ItemActionType>> ITEM_ACTION_TYPE = create(ApoliRegistryKeys.ITEM_ACTION_TYPE);

    public static final Registry<IModifierOperation> MODIFIER_OPERATION = create(ApoliRegistryKeys.MODIFIER_OPERATION);
    public static final Registry<ContainerType> CONTAINER_TYPE = create(ApoliRegistryKeys.CONTAINER_TYPE);

    private static <T> Registry<T> create(RegistryKey<Registry<T>> registryKey) {
        return FabricRegistryBuilder.createSimple(registryKey).buildAndRegister();
    }

}
