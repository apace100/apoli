package io.github.apace100.apoli.action.factory;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.type.entity.*;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Function;

public class EntityActions {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static void register() {

        MetaActions.register(ApoliDataTypes.ENTITY_ACTION, ApoliDataTypes.ENTITY_CONDITION, Function.identity(), EntityActions::register);

        register(DamageActionType.getFactory());
        register(HealActionType.getFactory());
        register(PlaySoundActionType.getFactory());
        register(ExhaustActionType.getFactory());
        register(ApplyEffectActionType.getFactory());
        register(ClearEffectActionType.getFactory());
        register(SetOnFireActionType.getFactory());
        register(AddVelocityActionType.getFactory());
        register(SpawnEntityActionType.getFactory());
        register(GainAirActionType.getFactory());
        register(BlockActionAtActionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("extinguish"), Entity::extinguish));
        register(ExecuteCommandActionType.getFactory());
        register(ChangeResourceActionType.getFactory());
        register(FeedActionType.getFactory());
        register(AddXpActionType.getFactory());
        register(SetFallDistanceActionType.getFactory());
        register(GiveActionType.getFactory());
        register(EquippedItemActionType.getFactory());
        register(TriggerCooldownActionType.getFactory());
        register(ToggleActionType.getFactory());
        register(EmitGameEventActionType.getFactory());
        register(SetResourceActionType.getFactory());
        register(GrantPowerActionType.getFactory());
        register(RevokePowerActionType.getFactory());
        register(RevokeAllPowersActionType.getFactory());
        register(RemovePowerActionType.getFactory());
        register(ExplodeActionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("dismount"), Entity::stopRiding));
        register(PassengerActionType.getFactory());
        register(RidingActionType.getFactory());
        register(AreaOfEffectActionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("crafting_table"), CraftingTableActionType::action));
        register(createSimpleFactory(Apoli.identifier("ender_chest"), EnderChestActionType::action));
        register(SwingHandActionType.getFactory());
        register(RaycastActionType.getFactory());
        register(SpawnParticlesActionType.getFactory());
        register(ModifyInventoryActionType.getFactory());
        register(ReplaceInventoryActionType.getFactory());
        register(DropInventoryActionType.getFactory());
        register(ModifyDeathTicksActionType.getFactory());
        register(ModifyResourceActionType.getFactory());
        register(ModifyStatActionType.getFactory());
        register(FireProjectileActionType.getFactory());
        register(SelectorActionType.getFactory());
        register(GrantAdvancementActionType.getFactory());
        register(RevokeAdvancementActionType.getFactory());
        register(ActionOnEntitySetActionType.getFactory());
        register(RandomTeleportActionType.getFactory());
        register(ShowToastActionType.getFactory());
        register(SpawnEffectCloudActionType.getFactory());

    }

    public static ActionTypeFactory<Entity> createSimpleFactory(Identifier id, Consumer<Entity> action) {
        return new ActionTypeFactory<>(id, new SerializableData(), (data, entity) -> action.accept(entity));
    }

    public static <F extends ActionTypeFactory<Entity>> F register(F actionFactory) {
        return Registry.register(ApoliRegistries.ENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }

}
