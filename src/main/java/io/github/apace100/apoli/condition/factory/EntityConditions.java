package io.github.apace100.apoli.condition.factory;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.type.entity.*;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.EntityAccessor;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.function.Predicate;

public class EntityConditions {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static void register() {
        MetaConditions.register(ApoliDataTypes.ENTITY_CONDITION, EntityConditions::register);
        register(BlockCollisionConditionType.getFactory());
        register(BrightnessConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("daytime"), entity -> TimeOfDayConditionType.condition(entity.getWorld(), Comparison.LESS_THAN, 13000)));
        register(TimeOfDayConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("fall_flying"), entity -> entity instanceof LivingEntity living && living.isFallFlying()));
        register(createSimpleFactory(Apoli.identifier("exposed_to_sun"), ExposedToSunConditionType::condition));
        register(createSimpleFactory(Apoli.identifier("in_rain"), entity -> ((EntityAccessor) entity).callIsBeingRainedOn()));
        register(createSimpleFactory(Apoli.identifier("invisible"), Entity::isInvisible));
        register(createSimpleFactory(Apoli.identifier("on_fire"), Entity::isOnFire));
        register(createSimpleFactory(Apoli.identifier("exposed_to_sky"), ExposedToSkyConditionType::condition));
        register(createSimpleFactory(Apoli.identifier("sneaking"), Entity::isSneaking));
        register(createSimpleFactory(Apoli.identifier("sprinting"), Entity::isSprinting));
        register(PowerActiveConditionType.getFactory());
        register(SubmergedInConditionType.getFactory());
        register(FluidHeightConditionType.getFactory());
        register(PowerConditionType.getFactory());
        register(FoodLevelConditionType.getFactory());
        register(SaturationLevelConditionType.getFactory());
        register(OnBlockConditionType.getFactory());
        register(EquippedConditionType.getFactory());
        register(AttributeConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("swimming"), Entity::isSwimming));
        register(ResourceConditionType.getFactory());
        register(AirConditionType.getFactory());
        register(InBlockConditionType.getFactory());
        register(BlockInRadiusConditionType.getFactory());
        DistanceFromCoordinatesConditionRegistry.registerEntityCondition(EntityConditions::register);
        register(DimensionConditionType.getFactory());
        register(XpLevelsConditionType.getFactory());
        register(XpPointsConditionType.getFactory());
        register(HealthConditionType.getFactory());
        register(RelativeHealthConditionType.getFactory());
        register(BiomeConditionType.getFactory());
        register(EntityTypeConditionType.getFactory());
        register(ScoreboardConditionType.getFactory());
        register(StatusEffectConditionType.getFactory());
        register(CommandConditionType.getFactory());
        register(PredicateConditionType.getFactory());
        register(FallDistanceConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("collided_horizontally"), entity -> entity.horizontalCollision));
        register(InBlockAnywhereConditionType.getFactory());
        register(InTagConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("climbing"), entity -> entity instanceof LivingEntity living && living.isClimbing()));
        register(createSimpleFactory(Apoli.identifier("tamed"), entity -> entity instanceof Tameable tameable && tameable.getOwnerUuid() != null));
        register(UsingItemConditionType.getFactory());
        register(MovingConditionType.getFactory());
        register(EnchantmentConditionType.getFactory());
        register(RidingConditionType.getFactory());
        register(RidingRootConditionType.getFactory());
        register(RidingRecursiveConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("living"), entity -> entity instanceof LivingEntity));
        register(PassengerConditionType.getFactory());
        register(PassengerRecursiveConditionType.getFactory());
        register(NbtConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("exists"), Objects::nonNull));
        register(createSimpleFactory(Apoli.identifier("creative_flying"), entity -> entity instanceof PlayerEntity player && player.getAbilities().flying));
        register(PowerTypeConditionType.getFactory());
        register(AbilityConditionType.getFactory());
        register(RaycastConditionType.getFactory());
        register(ElytraFlightPossibleConditionType.getFactory());
        register(InventoryConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("in_snow"), InSnowConditionType::condition));
        register(createSimpleFactory(Apoli.identifier("in_thunderstorm"), InThunderstormConditionType::condition));
        register(AdvancementConditionType.getFactory());
        register(EntitySetSizeConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("using_effective_tool"), UsingEffectiveToolConditionType::condition));
        register(GameModeConditionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("glowing"), entity -> !entity.getWorld().isClient ? entity.isGlowing() : MinecraftClient.getInstance().hasOutline(entity)));
        register(EntityInRadiusConditionType.getFactory());
        register(HasCommandTagConditionType.getFactory());
    }

    public static ConditionTypeFactory<Entity> createSimpleFactory(Identifier id, Predicate<Entity> condition) {
        return new ConditionTypeFactory<>(id, new SerializableData(), (data, entity) -> condition.test(entity));
    }

    public static <F extends ConditionTypeFactory<Entity>> F register(F conditionFactory) {
        return Registry.register(ApoliRegistries.ENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }

}
