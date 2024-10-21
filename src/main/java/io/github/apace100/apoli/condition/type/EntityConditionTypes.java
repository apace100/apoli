package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.type.entity.*;
import io.github.apace100.apoli.condition.type.entity.meta.AllOfEntityConditionType;
import io.github.apace100.apoli.condition.type.entity.meta.AnyOfEntityConditionType;
import io.github.apace100.apoli.condition.type.entity.meta.ConstantEntityConditionType;
import io.github.apace100.apoli.condition.type.entity.meta.RandomChanceEntityConditionType;
import io.github.apace100.apoli.condition.type.meta.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.registry.Registry;

public class EntityConditionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();
    public static final SerializableDataType<ConditionConfiguration<EntityConditionType>> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.ENTITY_CONDITION_TYPE, Apoli.MODID, ALIASES, (configurations, id) -> "Entity condition type \"" + id + "\" is not undefined!");

    public static final ConditionConfiguration<AllOfEntityConditionType> ALL_OF = register(AllOfMetaConditionType.createConfiguration(EntityCondition.DATA_TYPE, AllOfEntityConditionType::new));
    public static final ConditionConfiguration<AnyOfEntityConditionType> ANY_OF = register(AnyOfMetaConditionType.createConfiguration(EntityCondition.DATA_TYPE, AnyOfEntityConditionType::new));
    public static final ConditionConfiguration<ConstantEntityConditionType> CONSTANT = register(ConstantMetaConditionType.createConfiguration(ConstantEntityConditionType::new));
    public static final ConditionConfiguration<RandomChanceEntityConditionType> RANDOM_CHANCE = register(RandomChanceMetaConditionType.createConfiguration(RandomChanceEntityConditionType::new));

    public static final ConditionConfiguration<AbilityEntityConditionType> ABILITY = register(ConditionConfiguration.of(Apoli.identifier("ability"), AbilityEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<AdvancementEntityConditionType> ADVANCEMENT = register(ConditionConfiguration.of(Apoli.identifier("advancement"), AdvancementEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<AirEntityConditionType> AIR = register(ConditionConfiguration.of(Apoli.identifier("air"), AirEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<AttributeEntityConditionType> ATTRIBUTE = register(ConditionConfiguration.of(Apoli.identifier("attribute"), AttributeEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<BiomeEntityConditionType> BIOME = register(ConditionConfiguration.of(Apoli.identifier("biome"), BiomeEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<BlockCollisionEntityConditionType> BLOCK_COLLISION = register(ConditionConfiguration.of(Apoli.identifier("block_collision"), BlockCollisionEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<BlockInRadiusEntityConditionType> BLOCK_IN_RADIUS = register(ConditionConfiguration.of(Apoli.identifier("block_in_radius"), BlockInRadiusEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<BrightnessEntityConditionType> BRIGHTNESS = register(ConditionConfiguration.of(Apoli.identifier("brightness"), BrightnessEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<ClimbingEntityConditionType> CLIMBING = register(ConditionConfiguration.simple(Apoli.identifier("climbing"), ClimbingEntityConditionType::new));
    public static final ConditionConfiguration<CollidedHorizontallyEntityConditionType> COLLIDED_HORIZONTALLY = register(ConditionConfiguration.simple(Apoli.identifier("collided_horizontally"), CollidedHorizontallyEntityConditionType::new));
    public static final ConditionConfiguration<CommandEntityConditionType> COMMAND = register(ConditionConfiguration.of(Apoli.identifier("command"), CommandEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<CreativeFlyingEntityConditionType> CREATIVE_FLYING = register(ConditionConfiguration.simple(Apoli.identifier("creative_flying"), CreativeFlyingEntityConditionType::new));
    public static final ConditionConfiguration<DayTimeEntityConditionType> DAY_TIME = register(ConditionConfiguration.simple(Apoli.identifier("daytime"), DayTimeEntityConditionType::new));
    public static final ConditionConfiguration<DimensionEntityConditionType> DIMENSION = register(ConditionConfiguration.of(Apoli.identifier("dimension"), DimensionEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<DistanceFromCoordinatesEntityConditionType> DISTANCE_FROM_COORDINATES = register(DistanceFromCoordinatesMetaConditionType.createConfiguration(DistanceFromCoordinatesEntityConditionType::new));
    public static final ConditionConfiguration<ElytraFlightPossibleEntityConditionType> ELYTRA_FLIGHT_POSSIBLE = register(ConditionConfiguration.of(Apoli.identifier("elytra_flight_possible"), ElytraFlightPossibleEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<EnchantmentEntityConditionType> ENCHANTMENT = register(ConditionConfiguration.of(Apoli.identifier("enchantment"), EnchantmentEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<EntityInRadiusEntityConditionType> ENTITY_IN_RADIUS = register(ConditionConfiguration.of(Apoli.identifier("entity_in_radius"), EntityInRadiusEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<EntitySetSizeEntityConditionType> ENTITY_SET_SIZE = register(ConditionConfiguration.of(Apoli.identifier("entity_set_size"), EntitySetSizeEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<EntityTypeEntityConditionType> ENTITY_TYPE = register(ConditionConfiguration.of(Apoli.identifier("entity_type"), EntityTypeEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<EquippedItemEntityConditionType> EQUIPPED_ITEM = register(ConditionConfiguration.of(Apoli.identifier("equipped_item"), EquippedItemEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<ExistsEntityConditionType> EXISTS = register(ConditionConfiguration.simple(Apoli.identifier("exists"), ExistsEntityConditionType::new));
    public static final ConditionConfiguration<ExposedToSkyEntityConditionType> EXPOSED_TO_SKY = register(ConditionConfiguration.simple(Apoli.identifier("exposed_to_sky"), ExposedToSkyEntityConditionType::new));
    public static final ConditionConfiguration<ExposedToSunEntityConditionType> EXPOSED_TO_SUN = register(ConditionConfiguration.simple(Apoli.identifier("exposed_to_sun"), ExposedToSunEntityConditionType::new));
    public static final ConditionConfiguration<FallDistanceEntityConditionType> FALL_DISTANCE = register(ConditionConfiguration.of(Apoli.identifier("fall_distance"), FallDistanceEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<FallFlyingEntityConditionType> FALL_FLYING = register(ConditionConfiguration.simple(Apoli.identifier("fall_flying"), FallFlyingEntityConditionType::new));
    public static final ConditionConfiguration<FluidHeightEntityConditionType> FLUID_HEIGHT = register(ConditionConfiguration.of(Apoli.identifier("fluid_height"), FluidHeightEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<FoodLevelEntityConditionType> FOOD_LEVEL = register(ConditionConfiguration.of(Apoli.identifier("food_level"), FoodLevelEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<GameModeEntityConditionType> GAME_MODE = register(ConditionConfiguration.of(Apoli.identifier("gamemode"), GameModeEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<GlowingEntityConditionType> GLOWING = register(ConditionConfiguration.simple(Apoli.identifier("glowing"), GlowingEntityConditionType::new));
    public static final ConditionConfiguration<HasCommandTagEntityConditionType> HAS_COMMAND_TAG = register(ConditionConfiguration.of(Apoli.identifier("has_command_tag"), HasCommandTagEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<HealthEntityConditionType> HEALTH = register(ConditionConfiguration.of(Apoli.identifier("health"), HealthEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<InBlockAnywhereEntityConditionType> IN_BLOCK_ANYWHERE = register(ConditionConfiguration.of(Apoli.identifier("in_block_anywhere"), InBlockAnywhereEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<InBlockEntityConditionType> IN_BLOCK = register(ConditionConfiguration.of(Apoli.identifier("in_block"), InBlockEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<InRainEntityConditionType> IN_RAIN = register(ConditionConfiguration.simple(Apoli.identifier("in_rain"), InRainEntityConditionType::new));
    public static final ConditionConfiguration<InSnowEntityConditionType> IN_SNOW = register(ConditionConfiguration.simple(Apoli.identifier("in_snow"), InSnowEntityConditionType::new));
    public static final ConditionConfiguration<InTagEntityConditionType> IN_TAG = register(ConditionConfiguration.of(Apoli.identifier("in_tag"), InTagEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<InThunderstormEntityConditionType> IN_THUNDERSTORM = register(ConditionConfiguration.simple(Apoli.identifier("in_thunderstorm"), InThunderstormEntityConditionType::new));
    public static final ConditionConfiguration<InventoryEntityConditionType> INVENTORY = register(ConditionConfiguration.of(Apoli.identifier("inventory"), InventoryEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<InvisibleEntityConditionType> INVISIBLE = register(ConditionConfiguration.simple(Apoli.identifier("invisible"), InvisibleEntityConditionType::new));
    public static final ConditionConfiguration<LivingEntityConditionType> LIVING = register(ConditionConfiguration.simple(Apoli.identifier("living"), LivingEntityConditionType::new));
    public static final ConditionConfiguration<MovingEntityConditionType> MOVING = register(ConditionConfiguration.of(Apoli.identifier("moving"), MovingEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<NbtEntityConditionType> NBT = register(ConditionConfiguration.of(Apoli.identifier("nbt"), NbtEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<OnBlockEntityConditionType> ON_BLOCK = register(ConditionConfiguration.of(Apoli.identifier("on_block"), OnBlockEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<OnFireEntityConditionType> ON_FIRE = register(ConditionConfiguration.simple(Apoli.identifier("on_fire"), OnFireEntityConditionType::new));
    public static final ConditionConfiguration<PassengerEntityConditionType> PASSENGER = register(ConditionConfiguration.of(Apoli.identifier("passenger"), PassengerEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<PassengerRecursiveEntityConditionType> PASSENGER_RECURSIVE = register(ConditionConfiguration.of(Apoli.identifier("passenger_recursive"), PassengerRecursiveEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<PowerActiveEntityConditionType> POWER_ACTIVE = register(ConditionConfiguration.of(Apoli.identifier("power_active"), PowerActiveEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<PowerEntityConditionType> POWER = register(ConditionConfiguration.of(Apoli.identifier("power"), PowerEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<PowerTypeEntityConditionType> POWER_TYPE = register(ConditionConfiguration.of(Apoli.identifier("power_type"), PowerTypeEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<PredicateEntityConditionType> PREDICATE = register(ConditionConfiguration.of(Apoli.identifier("predicate"), PredicateEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<RaycastEntityConditionType> RAYCAST = register(ConditionConfiguration.of(Apoli.identifier("raycast"), RaycastEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<RelativeHealthEntityConditionType> RELATIVE_HEALTH = register(ConditionConfiguration.of(Apoli.identifier("relative_health"), RelativeHealthEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<ResourceEntityConditionType> RESOURCE = register(ConditionConfiguration.of(Apoli.identifier("resource"), ResourceEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<RidingEntityConditionType> RIDING = register(ConditionConfiguration.of(Apoli.identifier("riding"), RidingEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<RidingRecursiveEntityConditionType> RIDING_RECURSIVE = register(ConditionConfiguration.of(Apoli.identifier("riding_recursive"), RidingRecursiveEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<RidingRootEntityConditionType> RIDING_ROOT = register(ConditionConfiguration.of(Apoli.identifier("riding_root"), RidingRootEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<SaturationLevelEntityConditionType> SATURATION_LEVEL = register(ConditionConfiguration.of(Apoli.identifier("saturation_level"), SaturationLevelEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<ScoreboardEntityConditionType> SCOREBOARD = register(ConditionConfiguration.of(Apoli.identifier("scoreboard"), ScoreboardEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<SneakingEntityConditionType> SNEAKING = register(ConditionConfiguration.simple(Apoli.identifier("sneaking"), SneakingEntityConditionType::new));
    public static final ConditionConfiguration<SprintingEntityConditionType> SPRINTING = register(ConditionConfiguration.simple(Apoli.identifier("sprinting"), SprintingEntityConditionType::new));
    public static final ConditionConfiguration<StatusEffectEntityConditionType> STATUS_EFFECT = register(ConditionConfiguration.of(Apoli.identifier("status_effect"), StatusEffectEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<SubmergedInEntityConditionType> SUBMERGED_IN = register(ConditionConfiguration.of(Apoli.identifier("submerged_in"), SubmergedInEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<SwimmingEntityConditionType> SWIMMING = register(ConditionConfiguration.simple(Apoli.identifier("swimming"), SwimmingEntityConditionType::new));
    public static final ConditionConfiguration<TamedEntityConditionType> TAMED = register(ConditionConfiguration.simple(Apoli.identifier("tamed"), TamedEntityConditionType::new));
    public static final ConditionConfiguration<TimeOfDayEntityConditionType> TIME_OF_DAY = register(ConditionConfiguration.of(Apoli.identifier("time_of_day"), TimeOfDayEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<UsingEffectiveToolEntityConditionType> USING_EFFECTIVE_TOOL = register(ConditionConfiguration.simple(Apoli.identifier("using_effective_tool"), UsingEffectiveToolEntityConditionType::new));
    public static final ConditionConfiguration<UsingItemEntityConditionType> USING_ITEM = register(ConditionConfiguration.of(Apoli.identifier("using_item"), UsingItemEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<XpLevelsEntityConditionType> XP_LEVELS = register(ConditionConfiguration.of(Apoli.identifier("xp_levels"), XpLevelsEntityConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<XpPointsEntityConditionType> XP_POINTS = register(ConditionConfiguration.of(Apoli.identifier("xp_points"), XpPointsEntityConditionType.DATA_FACTORY));

	public static void register() {
//        DistanceFromCoordinatesConditionRegistry.registerEntityCondition(EntityConditionTypes::register);
    }

    @SuppressWarnings("unchecked")
	public static <CT extends EntityConditionType> ConditionConfiguration<CT> register(ConditionConfiguration<CT> configuration) {

        ConditionConfiguration<EntityConditionType> casted = (ConditionConfiguration<EntityConditionType>) configuration;
        Registry.register(ApoliRegistries.ENTITY_CONDITION_TYPE, casted.id(), casted);

        return configuration;

    }

}
