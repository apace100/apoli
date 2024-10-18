package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.type.entity.*;
import io.github.apace100.apoli.action.type.entity.meta.*;
import io.github.apace100.apoli.action.type.meta.*;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.registry.Registry;

public class EntityActionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();
    public static final SerializableDataType<ActionConfiguration<EntityActionType>> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.ENTITY_ACTION_TYPE, Apoli.MODID, ALIASES, (configurations, id) -> "Entity action type \"" + id + "\" is undefined!");

    public static final ActionConfiguration<AndEntityActionType> AND = register(AndMetaActionType.createConfiguration(EntityAction.DATA_TYPE, AndEntityActionType::new));
    public static final ActionConfiguration<ChanceEntityActionType> CHANCE = register(ChanceMetaActionType.createConfiguration(EntityAction.DATA_TYPE, ChanceEntityActionType::new));
    public static final ActionConfiguration<ChoiceEntityActionType> CHOICE = register(ChoiceMetaActionType.createConfiguration(EntityAction.DATA_TYPE, ChoiceEntityActionType::new));
    public static final ActionConfiguration<DelayEntityActionType> DELAY = register(DelayMetaActionType.createConfiguration(EntityAction.DATA_TYPE, DelayEntityActionType::new));
    public static final ActionConfiguration<IfElseListEntityActionType> IF_ELSE_LIST = register(IfElseListMetaActionType.createConfiguration(EntityAction.DATA_TYPE, EntityCondition.DATA_TYPE, IfElseListEntityActionType::new));
    public static final ActionConfiguration<IfElseEntityActionType> IF_ELSE = register(IfElseMetaActionType.createConfiguration(EntityAction.DATA_TYPE, EntityCondition.DATA_TYPE, IfElseEntityActionType::new));
    public static final ActionConfiguration<SideEntityActionType> SIDE = register(SideMetaActionType.createConfiguration(EntityAction.DATA_TYPE, SideEntityActionType::new));

    public static final ActionConfiguration<ActionOnEntitySetEntityActionType> ACTION_ON_ENTITY_SET = register(ActionConfiguration.fromDataFactory(Apoli.identifier("action_on_entity_set"), ActionOnEntitySetEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<AddVelocityEntityActionType> ADD_VELOCITY = register(ActionConfiguration.fromDataFactory(Apoli.identifier("add_velocity"), AddVelocityEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<AddXpEntityActionType> ADD_XP = register(ActionConfiguration.fromDataFactory(Apoli.identifier("add_xp"), AddXpEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ApplyEffectEntityActionType> APPLY_EFFECT = register(ActionConfiguration.fromDataFactory(Apoli.identifier("apply_effect"), ApplyEffectEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<AreaOfEffectEntityActionType> AREA_OF_EFFECT = register(ActionConfiguration.fromDataFactory(Apoli.identifier("area_of_effect"), AreaOfEffectEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<BlockActionAtEntityActionType> BLOCK_ACTION_AT = register(ActionConfiguration.fromDataFactory(Apoli.identifier("block_action_at"), BlockActionAtEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ChangeResourceEntityActionType> CHANGE_RESOURCE = register(ActionConfiguration.fromDataFactory(Apoli.identifier("change_resource"), ChangeResourceEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ClearEffectEntityActionType> CLEAR_EFFECT = register(ActionConfiguration.fromDataFactory(Apoli.identifier("clear_effect"), ClearEffectEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<CraftingTableEntityActionType> CRAFTING_TABLE = register(ActionConfiguration.simple(Apoli.identifier("crafting_table"), CraftingTableEntityActionType::new));
    public static final ActionConfiguration<DamageEntityActionType> DAMAGE = register(ActionConfiguration.fromDataFactory(Apoli.identifier("damage"), DamageEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<DismountEntityActionType> DISMOUNT = register(ActionConfiguration.simple(Apoli.identifier("dismount"), DismountEntityActionType::new));
    public static final ActionConfiguration<DropInventoryEntityActionType> DROP_INVENTORY = register(ActionConfiguration.fromDataFactory(Apoli.identifier("drop_inventory"), DropInventoryEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<EmitGameEventEntityActionType> EMIT_GAME_EVENT = register(ActionConfiguration.fromDataFactory(Apoli.identifier("emit_game_event"), EmitGameEventEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<EnderChestEntityActionType> ENDER_CHEST = register(ActionConfiguration.simple(Apoli.identifier("ender_chest"), EnderChestEntityActionType::new));
    public static final ActionConfiguration<EquippedItemActionEntityActionType> EQUIPPED_ITEM_ACTION = register(ActionConfiguration.fromDataFactory(Apoli.identifier("equipped_item_action"), EquippedItemActionEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ExecuteCommandEntityActionType> EXECUTE_COMMAND = register(ActionConfiguration.fromDataFactory(Apoli.identifier("execute_command"), ExecuteCommandEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ExhaustEntityActionType> EXHAUST = register(ActionConfiguration.fromDataFactory(Apoli.identifier("exhaust"), ExhaustEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ExplodeEntityActionType> EXPLODE = register(ActionConfiguration.fromDataFactory(Apoli.identifier("explode"), ExplodeEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ExtinguishEntityActionType> EXTINGUISH = register(ActionConfiguration.simple(Apoli.identifier("extinguish"), ExtinguishEntityActionType::new));
    public static final ActionConfiguration<FeedEntityActionType> FEED = register(ActionConfiguration.fromDataFactory(Apoli.identifier("feed"), FeedEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<FireProjectileEntityActionType> FIRE_PROJECTILE = register(ActionConfiguration.fromDataFactory(Apoli.identifier("fire_projectile"), FireProjectileEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<GainAirEntityActionType> GAIN_AIR = register(ActionConfiguration.fromDataFactory(Apoli.identifier("gain_air"), GainAirEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<GiveEntityActionType> GIVE = register(ActionConfiguration.fromDataFactory(Apoli.identifier("give"), GiveEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<GrantAdvancementEntityActionType> GRANT_ADVANCEMENT = register(ActionConfiguration.fromDataFactory(Apoli.identifier("grant_advancement"), GrantAdvancementEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<GrantPowerEntityActionType> GRANT_POWER = register(ActionConfiguration.fromDataFactory(Apoli.identifier("grant_power"), GrantPowerEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<HealEntityActionType> HEAL = register(ActionConfiguration.fromDataFactory(Apoli.identifier("heal"), HealEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ModifyDeathTicksEntityActionType> MODIFY_DEATH_TICKS = register(ActionConfiguration.fromDataFactory(Apoli.identifier("modify_death_ticks"), ModifyDeathTicksEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ModifyInventoryEntityActionType> MODIFY_INVENTORY = register(ActionConfiguration.fromDataFactory(Apoli.identifier("modify_inventory"), ModifyInventoryEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ModifyResourceEntityActionType> MODIFY_RESOURCE = register(ActionConfiguration.fromDataFactory(Apoli.identifier("modify_resource"), ModifyResourceEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ModifyStatEntityActionType> MODIFY_STAT = register(ActionConfiguration.fromDataFactory(Apoli.identifier("modify_stat"), ModifyStatEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<PassengerActionEntityActionType> PASSENGER = register(ActionConfiguration.fromDataFactory(Apoli.identifier("passenger_action"), PassengerActionEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<PlaySoundEntityActionType> PLAY_SOUND = register(ActionConfiguration.fromDataFactory(Apoli.identifier("play_sound"), PlaySoundEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<RandomTeleportEntityActionType> RANDOM_TELEPORT = register(ActionConfiguration.fromDataFactory(Apoli.identifier("random_teleport"), RandomTeleportEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<RaycastEntityActionType> RAYCAST = register(ActionConfiguration.fromDataFactory(Apoli.identifier("raycast"), RaycastEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<RemovePowerEntityActionType> REMOVE_POWER = register(ActionConfiguration.fromDataFactory(Apoli.identifier("remove_power"), RemovePowerEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ReplaceInventoryEntityActionType> REPLACE_INVENTORY = register(ActionConfiguration.fromDataFactory(Apoli.identifier("replace_inventory"), ReplaceInventoryEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<RevokeAdvancementEntityActionType> REVOKE_ADVANCEMENT = register(ActionConfiguration.fromDataFactory(Apoli.identifier("revoke_advancement"), RevokeAdvancementEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<RevokeAllPowersEntityActionType> REVOKE_ALL_POWERS = register(ActionConfiguration.fromDataFactory(Apoli.identifier("revoke_all_powers"), RevokeAllPowersEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<RevokePowerEntityActionType> REVOKE_POWER = register(ActionConfiguration.fromDataFactory(Apoli.identifier("revoke_power"), RevokePowerEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<RidingActionEntityActionType> RIDING_ACTION = register(ActionConfiguration.fromDataFactory(Apoli.identifier("riding_action"), RidingActionEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<SelectorActionEntityActionType> SELECTOR_ACTION = register(ActionConfiguration.fromDataFactory(Apoli.identifier("selector_action"), SelectorActionEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<SetFallDistanceEntityActionType> SET_FALL_DISTANCE = register(ActionConfiguration.fromDataFactory(Apoli.identifier("set_fall_distance"), SetFallDistanceEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<SetOnFireEntityActionType> SET_ON_FIRE = register(ActionConfiguration.fromDataFactory(Apoli.identifier("set_on_fire"), SetOnFireEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<SetResourceEntityActionType> SET_RESOURCE = register(ActionConfiguration.fromDataFactory(Apoli.identifier("set_resource"), SetResourceEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ShowToastEntityActionType> SHOW_TOAST = register(ActionConfiguration.fromDataFactory(Apoli.identifier("show_toast"), ShowToastEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<SpawnEffectCloudEntityActionType> SPAWN_EFFECT_CLOUD = register(ActionConfiguration.fromDataFactory(Apoli.identifier("spawn_effect_cloud"), SpawnEffectCloudEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<SpawnEntityEntityActionType> SPAWN_ENTITY = register(ActionConfiguration.fromDataFactory(Apoli.identifier("spawn_entity"), SpawnEntityEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<SpawnParticlesEntityActionType> SPAWN_PARTICLES = register(ActionConfiguration.fromDataFactory(Apoli.identifier("spawn_particles"), SpawnParticlesEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<SwingHandEntityActionType> SWING_HAND = register(ActionConfiguration.fromDataFactory(Apoli.identifier("swing_hand"), SwingHandEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<ToggleEntityActionType> TOGGLE = register(ActionConfiguration.fromDataFactory(Apoli.identifier("toggle"), ToggleEntityActionType.DATA_FACTORY));
    public static final ActionConfiguration<TriggerCooldownEntityActionType> TRIGGER_COOLDOWN = register(ActionConfiguration.fromDataFactory(Apoli.identifier("trigger_cooldown"), TriggerCooldownEntityActionType.DATA_FACTORY));

    public static void register() {

    }

    @SuppressWarnings("unchecked")
	public static <T extends EntityActionType> ActionConfiguration<T> register(ActionConfiguration<T> configuration) {

        ActionConfiguration<EntityActionType> casted = (ActionConfiguration<EntityActionType>) configuration;
        Registry.register(ApoliRegistries.ENTITY_ACTION_TYPE, casted.id(), casted);

        return configuration;

    }

}
