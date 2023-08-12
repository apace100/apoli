package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MovingEntity;
import io.github.apace100.apoli.access.SubmergableEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.EntityAccessor;
import io.github.apace100.apoli.power.ClimbingPower;
import io.github.apace100.apoli.power.ModifyEnchantmentLevelPower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.factory.condition.entity.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.function.Predicate;

public class EntityConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        MetaConditions.register(ApoliDataTypes.ENTITY_CONDITION, EntityConditions::register);
        register(BlockCollisionCondition.getFactory());
        register(new ConditionFactory<>(Apoli.identifier("brightness"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(entity.getBrightnessAtEyes(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("daytime"), new SerializableData(), (data, entity) -> entity.getWorld().getTimeOfDay() % 24000L < 13000L));
        register(new ConditionFactory<>(Apoli.identifier("time_of_day"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT), (data, entity) ->
            ((Comparison)data.get("comparison")).compare(entity.getWorld().getTimeOfDay() % 24000L, data.getInt("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("fall_flying"), new SerializableData(), (data, entity) -> entity instanceof LivingEntity && ((LivingEntity) entity).isFallFlying()));
        register(new ConditionFactory<>(Apoli.identifier("exposed_to_sun"), new SerializableData(), (data, entity) -> {
            if (entity.getWorld().isDay() && !((EntityAccessor) entity).callIsBeingRainedOn()) {
                float f = entity.getBrightnessAtEyes();
                BlockPos blockPos = entity.getVehicle() instanceof BoatEntity ? (BlockPos.ofFloored(entity.getX(), (double) Math.round(entity.getY()), entity.getZ())).up() : BlockPos.ofFloored(entity.getX(), (double) Math.round(entity.getY()), entity.getZ());
                return f > 0.5F && entity.getWorld().isSkyVisible(blockPos);
            }
            return false;
        }));
        register(new ConditionFactory<>(Apoli.identifier("in_rain"), new SerializableData(), (data, entity) -> ((EntityAccessor) entity).callIsBeingRainedOn()));
        register(new ConditionFactory<>(Apoli.identifier("invisible"), new SerializableData(), (data, entity) -> entity.isInvisible()));
        register(new ConditionFactory<>(Apoli.identifier("on_fire"), new SerializableData(), (data, entity) -> entity.isOnFire()));
        register(new ConditionFactory<>(Apoli.identifier("exposed_to_sky"), new SerializableData(), (data, entity) -> {
            BlockPos blockPos = entity.getVehicle() instanceof BoatEntity ? (BlockPos.ofFloored(entity.getX(), (double) Math.round(entity.getY()), entity.getZ())).up() : BlockPos.ofFloored(entity.getX(), (double) Math.round(entity.getY()), entity.getZ());
            return entity.getWorld().isSkyVisible(blockPos);
        }));
        register(new ConditionFactory<>(Apoli.identifier("sneaking"), new SerializableData(), (data, entity) -> entity.isSneaking()));
        register(new ConditionFactory<>(Apoli.identifier("sprinting"), new SerializableData(), (data, entity) -> entity.isSprinting()));
        register(new ConditionFactory<>(Apoli.identifier("power_active"), new SerializableData().add("power", ApoliDataTypes.POWER_TYPE),
            (data, entity) -> ((PowerTypeReference<?>)data.get("power")).isActive(entity)));
        register(new ConditionFactory<>(Apoli.identifier("status_effect"), new SerializableData()
            .add("effect", SerializableDataTypes.STATUS_EFFECT)
            .add("min_amplifier", SerializableDataTypes.INT, 0)
            .add("max_amplifier", SerializableDataTypes.INT, Integer.MAX_VALUE)
            .add("min_duration", SerializableDataTypes.INT, 0)
            .add("max_duration", SerializableDataTypes.INT, Integer.MAX_VALUE),
            (data, entity) -> {
                StatusEffect effect = data.get("effect");
                if(entity instanceof LivingEntity living) {
                    if (living.hasStatusEffect(effect)) {
                        StatusEffectInstance instance = living.getStatusEffect(effect);
                        return instance.getDuration() <= data.getInt("max_duration") && instance.getDuration() >= data.getInt("min_duration")
                            && instance.getAmplifier() <= data.getInt("max_amplifier") && instance.getAmplifier() >= data.getInt("min_amplifier");
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("submerged_in"), new SerializableData().add("fluid", SerializableDataTypes.FLUID_TAG),
            (data, entity) -> ((SubmergableEntity)entity).apoli$isSubmergedInLoosely(data.get("fluid"))));
        register(new ConditionFactory<>(Apoli.identifier("fluid_height"), new SerializableData()
            .add("fluid", SerializableDataTypes.FLUID_TAG)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(((SubmergableEntity)entity).apoli$getFluidHeightLoosely(data.get("fluid")), data.getDouble("compare_to"))));
        register(PowerCondition.getFactory());
        register(new ConditionFactory<>(Apoli.identifier("food_level"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    return ((Comparison)data.get("comparison")).compare(((PlayerEntity)entity).getHungerManager().getFoodLevel(), data.getInt("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("saturation_level"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    return ((Comparison) data.get("comparison")).compare(((PlayerEntity)entity).getHungerManager().getSaturationLevel(), data.getFloat("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("on_block"), new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
            (data, entity) -> entity.isOnGround() &&
                (!data.isPresent("block_condition") || ((ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition")).test(
                    new CachedBlockPosition(entity.getWorld(), BlockPos.ofFloored(entity.getX(), entity.getBoundingBox().minY - 0.5000001D, entity.getZ()), true)))));
        register(new ConditionFactory<>(Apoli.identifier("equipped_item"), new SerializableData()
            .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT)
            .add("item_condition", ApoliDataTypes.ITEM_CONDITION),
            (data, entity) -> entity instanceof LivingEntity && ((ConditionFactory<ItemStack>.Instance) data.get("item_condition")).test(
                ((LivingEntity) entity).getEquippedStack(data.get("equipment_slot")))));
        register(new ConditionFactory<>(Apoli.identifier("attribute"), new SerializableData()
            .add("attribute", SerializableDataTypes.ATTRIBUTE)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, entity) -> {
                double attrValue = 0F;
                if(entity instanceof LivingEntity living) {
                    EntityAttributeInstance attributeInstance = living.getAttributeInstance(data.get("attribute"));
                    if(attributeInstance != null) {
                        attrValue = attributeInstance.getValue();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(attrValue, data.getDouble("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("swimming"), new SerializableData(), (data, entity) -> entity.isSwimming()));
        register(ResourceCondition.getFactory());
        register(new ConditionFactory<>(Apoli.identifier("air"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(entity.getAir(), data.getInt("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("in_block"), new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION),
            (data, entity) ->((ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition")).test(
                new CachedBlockPosition(entity.getWorld(), entity.getBlockPos(), true))));
        register(new ConditionFactory<>(Apoli.identifier("block_in_radius"), new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION)
            .add("radius", SerializableDataTypes.INT)
            .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
            .add("compare_to", SerializableDataTypes.INT, 1)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL),
            (data, entity) -> {
                Predicate<CachedBlockPosition> blockCondition = data.get("block_condition");
                int stopAt = -1;
                Comparison comparison = data.get("comparison");
                int compareTo = data.getInt("compare_to");
                switch(comparison) {
                    case EQUAL: case LESS_THAN_OR_EQUAL: case GREATER_THAN:
                        stopAt = compareTo + 1;
                        break;
                    case LESS_THAN: case GREATER_THAN_OR_EQUAL:
                        stopAt = compareTo;
                        break;
                }
                int count = 0;
                for(BlockPos pos : Shape.getPositions(entity.getBlockPos(), data.get("shape"), data.getInt("radius"))) {
                    if(blockCondition.test(new CachedBlockPosition(entity.getWorld(), pos, true))) {
                        count++;
                        if(count == stopAt) {
                            break;
                        }
                    }
                }
                return comparison.compare(count, compareTo);
            }));
        DistanceFromCoordinatesConditionRegistry.registerEntityCondition(EntityConditions::register);
        register(new ConditionFactory<>(Apoli.identifier("dimension"), new SerializableData()
            .add("dimension", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> entity.getWorld().getRegistryKey() == RegistryKey.of(RegistryKeys.WORLD, data.getId("dimension"))));
        register(new ConditionFactory<>(Apoli.identifier("xp_levels"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    return ((Comparison)data.get("comparison")).compare(((PlayerEntity)entity).experienceLevel, data.getInt("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("xp_points"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    return ((Comparison)data.get("comparison")).compare(((PlayerEntity)entity).totalExperience, data.getInt("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("health"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(entity instanceof LivingEntity ? ((LivingEntity)entity).getHealth() : 0f, data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("relative_health"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                float health = 0f;
                if(entity instanceof LivingEntity living) {
                    health = living.getHealth() / living.getMaxHealth();
                }
                return ((Comparison)data.get("comparison")).compare(health, data.getFloat("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("biome"), new SerializableData()
            .add("biome", SerializableDataTypes.IDENTIFIER, null)
            .add("biomes", SerializableDataTypes.IDENTIFIERS, null)
            .add("condition", ApoliDataTypes.BIOME_CONDITION, null),
            (data, entity) -> {
                RegistryEntry<Biome> biomeEntry = entity.getWorld().getBiome(entity.getBlockPos());
                Biome biome = biomeEntry.value();
                ConditionFactory<RegistryEntry<Biome>>.Instance condition = data.get("condition");
                if(data.isPresent("biome") || data.isPresent("biomes")) {
                    Identifier biomeId = entity.getWorld().getRegistryManager().get(RegistryKeys.BIOME).getId(biome);
                    if(data.isPresent("biome") && biomeId.equals(data.getId("biome"))) {
                        return condition == null || condition.test(biomeEntry);
                    }
                    if(data.isPresent("biomes") && ((List<Identifier>)data.get("biomes")).contains(biomeId)) {
                        return condition == null || condition.test(biomeEntry);
                    }
                    return false;
                }
                return condition == null || condition.test(biomeEntry);
            }));
        register(new ConditionFactory<>(Apoli.identifier("entity_type"), new SerializableData()
            .add("entity_type", SerializableDataTypes.ENTITY_TYPE),
            (data, entity) -> entity.getType() == data.get("entity_type")));
        register(ScoreboardCondition.getFactory());
        register(new ConditionFactory<>(Apoli.identifier("command"), new SerializableData()
            .add("command", SerializableDataTypes.STRING)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> {
                MinecraftServer server = entity.getWorld().getServer();
                if(server != null) {
                    boolean validOutput = !(entity instanceof ServerPlayerEntity) || ((ServerPlayerEntity)entity).networkHandler != null;
                    ServerCommandSource source = new ServerCommandSource(
                        Apoli.config.executeCommand.showOutput && validOutput ? entity : CommandOutput.DUMMY,
                        entity.getPos(),
                        entity.getRotationClient(),
                        entity.getWorld() instanceof ServerWorld ? (ServerWorld)entity.getWorld() : null,
                        Apoli.config.executeCommand.permissionLevel,
                        entity.getName().getString(),
                        entity.getDisplayName(),
                        server,
                        entity);
                    int output = server.getCommandManager().executeWithPrefix(source, data.getString("command"));
                    return ((Comparison)data.get("comparison")).compare(output, data.getInt("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("predicate"), new SerializableData()
            .add("predicate", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> {
                MinecraftServer server = entity.getWorld().getServer();
                if (server != null) {
                    LootCondition lootCondition = server.getLootManager().getElement(LootDataType.PREDICATES, data.get("predicate"));
                    if (lootCondition != null) {
                        LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder((ServerWorld) entity.getWorld())
                                .add(LootContextParameters.ORIGIN, entity.getPos())
                                .addOptional(LootContextParameters.THIS_ENTITY, entity)
                                .build(LootContextTypes.COMMAND);
                        LootContext lootContext = new LootContext.Builder(lootContextParameterSet).build(null);
                        return lootCondition.test(lootContext);
                    }
                }
                return false;
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("fall_distance"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(entity.fallDistance, data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("collided_horizontally"), new SerializableData(),
            (data, entity) -> entity.horizontalCollision));
        register(new ConditionFactory<>(Apoli.identifier("in_block_anywhere"), new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> {
                Predicate<CachedBlockPosition> blockCondition = data.get("block_condition");
                int stopAt = -1;
                Comparison comparison = data.get("comparison");
                int compareTo = data.getInt("compare_to");
                switch(comparison) {
                    case EQUAL: case LESS_THAN_OR_EQUAL: case GREATER_THAN: case NOT_EQUAL:
                        stopAt = compareTo + 1;
                        break;
                    case LESS_THAN: case GREATER_THAN_OR_EQUAL:
                        stopAt = compareTo;
                        break;
                }
                int count = 0;
                Box box = entity.getBoundingBox();
                BlockPos blockPos = BlockPos.ofFloored(box.minX + 0.001D, box.minY + 0.001D, box.minZ + 0.001D);
                BlockPos blockPos2 = BlockPos.ofFloored(box.maxX - 0.001D, box.maxY - 0.001D, box.maxZ - 0.001D);
                BlockPos.Mutable mutable = new BlockPos.Mutable();
                for(int i = blockPos.getX(); i <= blockPos2.getX() && count < stopAt; ++i) {
                    for(int j = blockPos.getY(); j <= blockPos2.getY() && count < stopAt; ++j) {
                        for(int k = blockPos.getZ(); k <= blockPos2.getZ() && count < stopAt; ++k) {
                            mutable.set(i, j, k);
                            if(blockCondition.test(new CachedBlockPosition(entity.getWorld(), mutable, true))) {
                                count++;
                            }
                        }
                    }
                }
                return comparison.compare(count, compareTo);}));
        register(new ConditionFactory<>(Apoli.identifier("entity_group"), new SerializableData()
            .add("group", SerializableDataTypes.ENTITY_GROUP),
            (data, entity) -> entity instanceof LivingEntity && ((LivingEntity) entity).getGroup() == data.get("group")));
        register(new ConditionFactory<>(Apoli.identifier("in_tag"), new SerializableData()
            .add("tag", SerializableDataTypes.ENTITY_TAG),
            (data, entity) -> entity.getType().getRegistryEntry().isIn(data.get("tag"))));
        register(new ConditionFactory<>(Apoli.identifier("climbing"), new SerializableData(),
            (data, entity) -> {
                if(entity instanceof LivingEntity && ((LivingEntity)entity).isClimbing()) {
                    return true;
                }
                if(PowerHolderComponent.hasPower(entity, ClimbingPower.class)) {
                    return true;
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("tamed"), new SerializableData(), (data, entity) -> {
            if(entity instanceof TameableEntity) {
                return ((TameableEntity)entity).isTamed();
            }
            return false;
        }));
        register(new ConditionFactory<>(Apoli.identifier("using_item"), new SerializableData()
            .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null), (data, entity) -> {
            if(entity instanceof LivingEntity living) {
                if (living.isUsingItem()) {
                    ConditionFactory<ItemStack>.Instance condition = data.get("item_condition");
                    if (condition != null) {
                        Hand activeHand = living.getActiveHand();
                        ItemStack handStack = living.getStackInHand(activeHand);
                        return condition.test(handStack);
                    } else {
                        return true;
                    }
                }
            }
            return false;
        }));
        register(new ConditionFactory<>(Apoli.identifier("moving"), new SerializableData(),
            (data, entity) -> ((MovingEntity)entity).apoli$isMoving()));
        register(new ConditionFactory<>(Apoli.identifier("enchantment"), new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT)
            .add("calculation", SerializableDataTypes.STRING, "sum")
            .add("use_modifications", SerializableDataTypes.BOOLEAN, true),
            (data, entity) -> {
                int value = 0;
                if(entity instanceof LivingEntity le) {
                    Enchantment enchantment = data.get("enchantment");
                    String calculation = data.getString("calculation");
                    switch(calculation) {
                        case "sum":
                            for(ItemStack stack : enchantment.getEquipment(le).values()) {
                                value += ModifyEnchantmentLevelPower.getLevel(le, enchantment, stack, data.getBoolean("use_modifications"));
                            }
                            break;
                        case "max":
                            value = ModifyEnchantmentLevelPower.getEquipmentLevel(enchantment, le, data.getBoolean("use_modifications"));
                            break;
                        default:
                            Apoli.LOGGER.error("Error in \"enchantment\" entity condition, undefined calculation type: \"" + calculation + "\".");
                            break;
                    }
                }
                return ((Comparison)data.get("comparison")).compare(value, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("riding"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            (data, entity) -> {
                if(entity.hasVehicle()) {
                    if(data.isPresent("bientity_condition")) {
                        Predicate<Pair<Entity, Entity>> condition = data.get("bientity_condition");
                        Entity vehicle = entity.getVehicle();
                        return condition.test(new Pair<>(entity, vehicle));
                    }
                    return true;
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("riding_root"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            (data, entity) -> {
                if(entity.hasVehicle()) {
                    if(data.isPresent("bientity_condition")) {
                        Predicate<Pair<Entity, Entity>> condition = data.get("bientity_condition");
                        Entity vehicle = entity.getRootVehicle();
                        return condition.test(new Pair<>(entity, vehicle));
                    }
                    return true;
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("riding_recursive"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> {
                int count = 0;
                if(entity.hasVehicle()) {
                    Predicate<Pair<Entity, Entity>> cond = data.get("bientity_condition");
                    Entity vehicle = entity.getVehicle();
                    while(vehicle != null) {
                        if(cond == null || cond.test(new Pair<>(entity, vehicle))) {
                            count++;
                        }
                        vehicle = vehicle.getVehicle();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(count, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("living"), new SerializableData(), (data, entity) -> entity instanceof LivingEntity));
        register(new ConditionFactory<>(Apoli.identifier("passenger"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> {
                int count = 0;
                if(entity.hasPassengers()) {
                    if(data.isPresent("bientity_condition")) {
                        Predicate<Pair<Entity, Entity>> condition = data.get("bientity_condition");
                        count = (int)entity.getPassengerList().stream().filter(e -> condition.test(new Pair<>(e, entity))).count();
                    } else {
                        count = entity.getPassengerList().size();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(count, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("passenger_recursive"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> {
                int count = 0;
                if(entity.hasPassengers()) {
                    if(data.isPresent("bientity_condition")) {
                        Predicate<Pair<Entity, Entity>> condition = data.get("bientity_condition");
                        List<Entity> passengers = entity.getPassengerList();
                        count = (int)passengers.stream().flatMap(Entity::streamSelfAndPassengers).filter(e -> condition.test(new Pair<>(e, entity))).count();
                    } else {
                        count = (int)entity.getPassengerList().stream().flatMap(Entity::streamSelfAndPassengers).count();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(count, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("nbt"), new SerializableData()
            .add("nbt", SerializableDataTypes.NBT),
            (data, entity) -> {
                NbtCompound nbt = new NbtCompound();
                entity.writeNbt(nbt);
                return NbtHelper.matches(data.get("nbt"), nbt, true);
            }));
        register(new ConditionFactory<>(Apoli.identifier("exists"), new SerializableData(), (data, entity) -> entity != null));
        register(new ConditionFactory<>(Apoli.identifier("creative_flying"), new SerializableData(),
            (data, entity) -> entity instanceof PlayerEntity && ((PlayerEntity)entity).getAbilities().flying));
        register(new ConditionFactory<>(Apoli.identifier("power_type"), new SerializableData()
            .add("power_type", ApoliDataTypes.POWER_TYPE),
            (data, entity) -> {
                PowerTypeReference<?> powerTypeReference = data.get("power_type");
                PowerType<?> powerType = powerTypeReference.getReferencedPowerType();
                return PowerHolderComponent.KEY.maybeGet(entity).map(phc -> phc.getPowerTypes(true).contains(powerType)).orElse(false);
            }));
        register(new ConditionFactory<>(Apoli.identifier("ability"), new SerializableData()
            .add("ability", ApoliDataTypes.PLAYER_ABILITY),
            (data, entity) -> {
                if(entity instanceof PlayerEntity && !entity.getWorld().isClient) {
                    return ((PlayerAbility) data.get("ability")).isEnabledFor((PlayerEntity) entity);
                }
                return false;
            }));
        register(RaycastCondition.getFactory());
        register(ElytraFlightPossibleCondition.getFactory());
        register(InventoryCondition.getFactory());
        register(InSnowCondition.getFactory());
        register(InThunderstormCondition.getFactory());
    }

    private static void register(ConditionFactory<Entity> conditionFactory) {
        Registry.register(ApoliRegistries.ENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
